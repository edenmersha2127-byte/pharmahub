package com.medicinelocator.inventory.application.service;

import com.medicinelocator.inventory.application.dto.response.InventoryStatsResponse;
import com.medicinelocator.inventory.application.dto.response.InventoryTrendsResponse;
import com.medicinelocator.inventory.application.dto.response.StockMovementSummaryResponse;
import com.medicinelocator.inventory.domain.enums.StockMovementType;
import com.medicinelocator.inventory.domain.model.InventorySnapshot;
import com.medicinelocator.inventory.domain.model.Medicine;
import com.medicinelocator.inventory.domain.model.MedicineBatch;
import com.medicinelocator.inventory.domain.model.StockMovement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryReportService")
class InventoryReportServiceTest {

    @Mock private MedicineService medicineService;
    @Mock private MedicineBatchRepository batchRepository;
    @Mock private StockMovementRepository stockMovementRepository;
    @Mock private InventorySnapshotRepository snapshotRepository;

    private InventoryReportService service;

    private final UUID pharmacyId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new InventoryReportService(
                medicineService, batchRepository, stockMovementRepository, snapshotRepository, 30);
    }

    @Test
    @DisplayName("stats includes stock-level totals computed over the whole catalogue")
    void stats_includesStockTotals() {
        when(medicineService.countActiveMedicines(pharmacyId)).thenReturn(10L);
        when(medicineService.findLowStock(pharmacyId)).thenReturn(List.of(new Medicine(), new Medicine()));
        when(batchRepository.findExpiringBetween(eq(pharmacyId), any(), any()))
                .thenReturn(List.of(new MedicineBatch()));
        when(batchRepository.findExpired(eq(pharmacyId), any())).thenReturn(List.of());
        when(medicineService.countInStock(pharmacyId)).thenReturn(7L);
        when(medicineService.sumStockUnits(pharmacyId)).thenReturn(500L);
        when(medicineService.sumInventoryValue(pharmacyId)).thenReturn(new BigDecimal("1234.56"));

        InventoryStatsResponse stats = service.stats(pharmacyId);

        assertEquals(10L, stats.totalMedicines());
        assertEquals(2L, stats.lowStockCount());
        assertEquals(1L, stats.expiringSoonCount());
        assertEquals(0L, stats.expiredCount());
        assertEquals(7L, stats.inStockCount());
        assertEquals(3L, stats.outOfStockCount());
        assertEquals(500L, stats.totalStockUnits());
        assertEquals(new BigDecimal("1234.56"), stats.totalInventoryValue());
    }

    @Test
    @DisplayName("trends computes percentage deltas against the baseline snapshot")
    void trends_withBaseline() {
        when(medicineService.countActiveMedicines(pharmacyId)).thenReturn(10L);
        when(medicineService.sumInventoryValue(pharmacyId)).thenReturn(new BigDecimal("1200"));
        when(snapshotRepository.findLatestOnOrBefore(eq(pharmacyId), any()))
                .thenReturn(Optional.of(new InventorySnapshot(
                        null, pharmacyId, null, 8L, new BigDecimal("1000"), null)));

        InventoryTrendsResponse trends = service.trends(pharmacyId);

        assertEquals(new BigDecimal("25.00"), trends.medicinesDeltaPct());
        assertEquals(new BigDecimal("20.00"), trends.inventoryValueDeltaPct());
    }

    @Test
    @DisplayName("trends returns null deltas when no baseline snapshot exists yet")
    void trends_withoutBaseline() {
        when(medicineService.countActiveMedicines(pharmacyId)).thenReturn(10L);
        when(medicineService.sumInventoryValue(pharmacyId)).thenReturn(new BigDecimal("1200"));
        when(snapshotRepository.findLatestOnOrBefore(eq(pharmacyId), any())).thenReturn(Optional.empty());

        InventoryTrendsResponse trends = service.trends(pharmacyId);

        assertNull(trends.medicinesDeltaPct());
        assertNull(trends.inventoryValueDeltaPct());
    }

    @Test
    @DisplayName("movementsSummary buckets PURCHASE as in and SALE as out, zero-filling the range")
    void movementsSummary_bucketsByType() {
        StockMovement purchase = new StockMovement(UUID.randomUUID(), UUID.randomUUID(), pharmacyId,
                StockMovementType.PURCHASE, 50, 50, "received", null, LocalDateTime.now());
        StockMovement sale = new StockMovement(UUID.randomUUID(), UUID.randomUUID(), pharmacyId,
                StockMovementType.SALE, -20, 30, "sold", null, LocalDateTime.now());
        when(stockMovementRepository.findByPharmacyIdBetween(eq(pharmacyId), any(), any()))
                .thenReturn(List.of(purchase, sale));

        List<StockMovementSummaryResponse> summary = service.movementsSummary(pharmacyId, 3);

        assertEquals(3, summary.size(), "should zero-fill all months in the window");
        StockMovementSummaryResponse current = summary.get(summary.size() - 1);
        assertEquals(YearMonth.now().toString(), current.period());
        assertEquals(50L, current.unitsIn());
        assertEquals(20L, current.unitsOut());
        assertEquals(30L, current.net());
        // Earlier (empty) months are present and zeroed.
        assertEquals(0L, summary.get(0).unitsIn());
        assertEquals(0L, summary.get(0).unitsOut());
    }
}
