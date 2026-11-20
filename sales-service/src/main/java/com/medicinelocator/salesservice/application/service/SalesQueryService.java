package com.medicinelocator.salesservice.application.service;

import com.medicinelocator.salesservice.application.dto.response.DailySummaryResponse;
import com.medicinelocator.salesservice.application.dto.response.SalesAnalyticsResponse;
import com.medicinelocator.salesservice.application.dto.response.SalesTimeseriesResponse;
import com.medicinelocator.salesservice.application.dto.response.TopMedicineResponse;
import com.medicinelocator.salesservice.domain.enums.Granularity;
import com.medicinelocator.salesservice.domain.exception.SaleNotFoundException;
import com.medicinelocator.salesservice.domain.model.Sale;
import com.medicinelocator.salesservice.domain.model.SaleItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Read-only queries over recorded sales: listing, single-sale lookup, per-
 * customer purchase history, today's summary, bucketed time series, and
 * aggregate analytics for the dashboard.
 */
@Service
public class SalesQueryService {

    private final SaleRepository saleRepository;

    public SalesQueryService(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Transactional(readOnly = true)
    public List<Sale> listSales(UUID pharmacyId) {
        return saleRepository.findByPharmacyId(pharmacyId);
    }

    @Transactional(readOnly = true)
    public Sale getSale(UUID pharmacyId, UUID saleId) {
        return saleRepository.findByIdAndPharmacyId(saleId, pharmacyId)
                .orElseThrow(() -> new SaleNotFoundException(pharmacyId, saleId));
    }

    @Transactional(readOnly = true)
    public List<Sale> customerPurchases(UUID pharmacyId, UUID customerId) {
        return saleRepository.findByCustomer(customerId, pharmacyId);
    }

    /** Today's sales figures plus the day-over-day revenue delta for the dashboard. */
    @Transactional(readOnly = true)
    public DailySummaryResponse todaySummary(UUID pharmacyId) {
        LocalDate today = LocalDate.now();
        List<Sale> todaySales = saleRepository.findBetween(
                pharmacyId, today.atStartOfDay(), today.atTime(LocalTime.MAX));
        long todayCount = todaySales.size();
        BigDecimal todayRevenue = sumTotals(todaySales);

        LocalDate yesterday = today.minusDays(1);
        BigDecimal previousDayRevenue = sumTotals(saleRepository.findBetween(
                pharmacyId, yesterday.atStartOfDay(), yesterday.atTime(LocalTime.MAX)));

        BigDecimal revenueDeltaPct = percentDelta(todayRevenue, previousDayRevenue);

        return new DailySummaryResponse(today, todayCount, todayRevenue,
                previousDayRevenue, revenueDeltaPct);
    }

    /**
     * Revenue, profit (sell - buy) and sale count over an inclusive date range.
     */
    @Transactional(readOnly = true)
    public SalesAnalyticsResponse analytics(UUID pharmacyId, LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        long salesCount = saleRepository.findBetween(pharmacyId, start, end).size();
        List<SaleItem> items = saleRepository.findItemsBetween(pharmacyId, start, end);

        BigDecimal revenue = items.stream()
                .map(SaleItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal profit = items.stream()
                .map(SaleItem::lineProfit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SalesAnalyticsResponse(from, to, salesCount, revenue, profit);
    }

    /**
     * Sales bucketed by day or month over an inclusive date range: per-bucket
     * sale count, revenue (Σ line totals) and profit (Σ line profit). Every
     * bucket in the range is present (zero-filled) so the trend chart is
     * continuous. Revenue/profit are derived from sale-item lines, matching
     * {@link #analytics}.
     */
    @Transactional(readOnly = true)
    public List<SalesTimeseriesResponse> timeseries(UUID pharmacyId, LocalDate from, LocalDate to,
                                                    Granularity granularity) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        // Seed every bucket in range so the series has no gaps.
        Map<String, Bucket> byPeriod = new LinkedHashMap<>();
        if (granularity == Granularity.MONTH) {
            YearMonth last = YearMonth.from(to);
            for (YearMonth ym = YearMonth.from(from); !ym.isAfter(last); ym = ym.plusMonths(1)) {
                byPeriod.put(ym.toString(), new Bucket());
            }
        } else {
            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                byPeriod.put(d.toString(), new Bucket());
            }
        }

        // Sale headers give us the bucket key per sale id, and the per-bucket sale count.
        List<Sale> sales = saleRepository.findBetween(pharmacyId, start, end);
        Map<UUID, String> periodBySale = new HashMap<>();
        for (Sale sale : sales) {
            if (sale.getCreatedAt() == null) {
                continue;
            }
            String period = periodKey(sale.getCreatedAt().toLocalDate(), granularity);
            periodBySale.put(sale.getId(), period);
            Bucket bucket = byPeriod.get(period);
            if (bucket != null) {
                bucket.salesCount++;
            }
        }

        // Revenue/profit come from the sale-item lines, bucketed via their parent sale.
        for (SaleItem item : saleRepository.findItemsBetween(pharmacyId, start, end)) {
            String period = periodBySale.get(item.getSaleId());
            if (period == null) {
                continue;
            }
            Bucket bucket = byPeriod.get(period);
            if (bucket != null) {
                bucket.revenue = bucket.revenue.add(item.lineTotal());
                bucket.profit = bucket.profit.add(item.lineProfit());
            }
        }

        return byPeriod.entrySet().stream()
                .map(e -> new SalesTimeseriesResponse(
                        e.getKey(), e.getValue().salesCount, e.getValue().revenue, e.getValue().profit))
                .toList();
    }

    /**
     * Best-selling medicines over a date range, ranked by units sold.
     */
    @Transactional(readOnly = true)
    public List<TopMedicineResponse> topMedicines(UUID pharmacyId, LocalDate from, LocalDate to, int limit) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        List<SaleItem> items = saleRepository.findItemsBetween(pharmacyId, start, end);

        record Agg(String name, long qty, BigDecimal revenue) {}
        Map<UUID, Agg> byMedicine = new HashMap<>();
        for (SaleItem item : items) {
            byMedicine.merge(item.getMedicineId(),
                    new Agg(item.getMedicineName(), item.getQuantity(), item.lineTotal()),
                    (a, b) -> new Agg(a.name() != null ? a.name() : b.name(),
                            a.qty() + b.qty(), a.revenue().add(b.revenue())));
        }

        return byMedicine.entrySet().stream()
                .map(e -> new TopMedicineResponse(
                        e.getKey(), e.getValue().name(), e.getValue().qty(), e.getValue().revenue()))
                .sorted(Comparator.comparingLong(TopMedicineResponse::quantitySold).reversed())
                .limit(limit)
                .toList();
    }

    private static BigDecimal sumTotals(List<Sale> sales) {
        return sales.stream()
                .map(Sale::getTotal)
                .filter(t -> t != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String periodKey(LocalDate date, Granularity granularity) {
        return granularity == Granularity.MONTH
                ? YearMonth.from(date).toString()
                : date.toString();
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

    /** Mutable per-bucket accumulator for {@link #timeseries}. */
    private static final class Bucket {
        long salesCount;
        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal profit = BigDecimal.ZERO;
    }
}
