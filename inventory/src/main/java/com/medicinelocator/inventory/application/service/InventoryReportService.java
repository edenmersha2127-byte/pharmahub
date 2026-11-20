package com.medicinelocator.inventory.application.service;

import com.medicinelocator.inventory.application.dto.response.InventoryStatsResponse;
import com.medicinelocator.inventory.application.dto.response.InventoryTrendsResponse;
import com.medicinelocator.inventory.application.dto.response.StockMovementSummaryResponse;
import com.medicinelocator.inventory.domain.enums.StockMovementType;
import com.medicinelocator.inventory.domain.model.InventorySnapshot;
import com.medicinelocator.inventory.domain.model.Medicine;
import com.medicinelocator.inventory.domain.model.MedicineBatch;
import com.medicinelocator.inventory.domain.model.StockMovement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Read-only inventory reporting: stock-movement history, per-medicine batches,
 * low-stock, expiry (soon / already expired), dashboard stats, pharmacy-wide
 * movement summaries, and month-over-month trend deltas. All queries are scoped
 * to the owning pharmacy.
 */
@Service
public class InventoryReportService {

    /** Trend deltas compare "now" against the snapshot nearest this many days ago. */
    private static final int TREND_BASELINE_DAYS = 30;

    private final MedicineService medicineService;
    private final MedicineBatchRepository batchRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InventorySnapshotRepository snapshotRepository;
    private final int expiryWarningDays;

    public InventoryReportService(MedicineService medicineService,
                                  MedicineBatchRepository batchRepository,
                                  StockMovementRepository stockMovementRepository,
                                  InventorySnapshotRepository snapshotRepository,
                                  @Value("${inventory.expiry-warning-days:30}") int expiryWarningDays) {
        this.medicineService = medicineService;
        this.batchRepository = batchRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.snapshotRepository = snapshotRepository;
        this.expiryWarningDays = expiryWarningDays;
    }

    @Transactional(readOnly = true)
    public List<StockMovement> stockMovements(UUID pharmacyId, UUID medicineId) {
        return stockMovementRepository.findByMedicineIdAndPharmacyId(medicineId, pharmacyId);
    }

    @Transactional(readOnly = true)
    public List<MedicineBatch> batches(UUID medicineId) {
        return batchRepository.findByMedicineIdFefo(medicineId);
    }

    @Transactional(readOnly = true)
    public List<Medicine> lowStock(UUID pharmacyId) {
        return medicineService.findLowStock(pharmacyId);
    }

    /** Batches expiring within {@code withinDays} days from today (inclusive). */
    @Transactional(readOnly = true)
    public List<MedicineBatch> expiringSoon(UUID pharmacyId, int withinDays) {
        LocalDate today = LocalDate.now();
        return batchRepository.findExpiringBetween(pharmacyId, today, today.plusDays(withinDays));
    }

    /** Batches whose expiry date is already in the past. */
    @Transactional(readOnly = true)
    public List<MedicineBatch> expired(UUID pharmacyId) {
        return batchRepository.findExpired(pharmacyId, LocalDate.now());
    }

    /** Dashboard counters for a pharmacy: catalogue size, stock/expiry alerts, stock totals. */
    @Transactional(readOnly = true)
    public InventoryStatsResponse stats(UUID pharmacyId) {
        LocalDate today = LocalDate.now();
        long total = medicineService.countActiveMedicines(pharmacyId);
        long lowStock = medicineService.findLowStock(pharmacyId).size();
        long expiringSoon = batchRepository
                .findExpiringBetween(pharmacyId, today, today.plusDays(expiryWarningDays)).size();
        long expired = batchRepository.findExpired(pharmacyId, today).size();
        long inStock = medicineService.countInStock(pharmacyId);
        long outOfStock = total - inStock;
        long totalStockUnits = medicineService.sumStockUnits(pharmacyId);
        BigDecimal totalInventoryValue = medicineService.sumInventoryValue(pharmacyId);
        return new InventoryStatsResponse(total, lowStock, expiringSoon, expired,
                inStock, outOfStock, totalStockUnits, totalInventoryValue);
    }

    /**
     * Pharmacy-wide stock movement totals bucketed by calendar month, over the
     * most recent {@code months} months (inclusive of the current month). Every
     * month in the window is present (zero-filled) so the chart is continuous.
     */
    @Transactional(readOnly = true)
    public List<StockMovementSummaryResponse> movementsSummary(UUID pharmacyId, int months) {
        YearMonth currentMonth = YearMonth.now();
        YearMonth firstMonth = currentMonth.minusMonths(months - 1L);
        LocalDateTime from = firstMonth.atDay(1).atStartOfDay();
        LocalDateTime to = LocalDateTime.now();

        // Seed every month in range so the series has no gaps.
        Map<String, long[]> byPeriod = new LinkedHashMap<>();
        for (YearMonth ym = firstMonth; !ym.isAfter(currentMonth); ym = ym.plusMonths(1)) {
            byPeriod.put(ym.toString(), new long[]{0L, 0L}); // [unitsIn, unitsOut]
        }

        for (StockMovement movement : stockMovementRepository.findByPharmacyIdBetween(pharmacyId, from, to)) {
            if (movement.getCreatedAt() == null) {
                continue;
            }
            String period = YearMonth.from(movement.getCreatedAt()).toString();
            long[] bucket = byPeriod.get(period);
            if (bucket == null) {
                continue; // defensive: outside the seeded window
            }
            long units = Math.abs((long) movement.getQuantityChange());
            if (isInbound(movement)) {
                bucket[0] += units;
            } else {
                bucket[1] += units;
            }
        }

        List<StockMovementSummaryResponse> summary = new ArrayList<>(byPeriod.size());
        for (Map.Entry<String, long[]> entry : byPeriod.entrySet()) {
            long in = entry.getValue()[0];
            long out = entry.getValue()[1];
            summary.add(new StockMovementSummaryResponse(entry.getKey(), in, out, in - out));
        }
        return summary;
    }

    /**
     * Percentage change of catalogue size and inventory value between now and the
     * baseline snapshot nearest {@link #TREND_BASELINE_DAYS} days ago. Deltas are
     * {@code null} when no baseline snapshot exists yet or the baseline was zero.
     */
    @Transactional(readOnly = true)
    public InventoryTrendsResponse trends(UUID pharmacyId) {
        long currentCount = medicineService.countActiveMedicines(pharmacyId);
        BigDecimal currentValue = medicineService.sumInventoryValue(pharmacyId);

        LocalDate baselineDate = LocalDate.now().minusDays(TREND_BASELINE_DAYS);
        Optional<InventorySnapshot> baseline = snapshotRepository.findLatestOnOrBefore(pharmacyId, baselineDate);

        BigDecimal medicinesDeltaPct = baseline
                .map(b -> percentDelta(BigDecimal.valueOf(currentCount), BigDecimal.valueOf(b.getMedicineCount())))
                .orElse(null);
        BigDecimal inventoryValueDeltaPct = baseline
                .map(b -> percentDelta(currentValue, b.getInventoryValue()))
                .orElse(null);

        return new InventoryTrendsResponse(medicinesDeltaPct, inventoryValueDeltaPct);
    }

    private boolean isInbound(StockMovement movement) {
        StockMovementType type = movement.getType();
        if (type == StockMovementType.PURCHASE || type == StockMovementType.INITIAL) {
            return true;
        }
        if (type == StockMovementType.SALE || type == StockMovementType.EXPIRED) {
            return false;
        }
        // ADJUSTMENT (or any future type): classify by the sign of the change.
        return movement.getQuantityChange() >= 0;
    }

    /** (current - base) / base × 100, 2dp; null when base is null or zero. */
    private static BigDecimal percentDelta(BigDecimal current, BigDecimal base) {
        if (base == null || base.signum() == 0) {
            return null;
        }
        BigDecimal safeCurrent = current != null ? current : BigDecimal.ZERO;
        return safeCurrent.subtract(base)
                .multiply(BigDecimal.valueOf(100))
                .divide(base, 2, RoundingMode.HALF_UP);
    }
}
