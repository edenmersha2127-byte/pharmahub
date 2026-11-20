package com.medicinelocator.salesservice.application.service;

import com.medicinelocator.salesservice.application.dto.response.DailySummaryResponse;
import com.medicinelocator.salesservice.application.dto.response.SalesTimeseriesResponse;
import com.medicinelocator.salesservice.domain.enums.Granularity;
import com.medicinelocator.salesservice.domain.model.Sale;
import com.medicinelocator.salesservice.domain.model.SaleItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SalesQueryService")
class SalesQueryServiceTest {

    @Mock private SaleRepository saleRepository;

    @InjectMocks private SalesQueryService service;

    private final UUID pharmacyId = UUID.randomUUID();

    private Sale sale(BigDecimal total, LocalDate day) {
        return new Sale(UUID.randomUUID(), pharmacyId, null, null, null, total, day.atStartOfDay());
    }

    @Test
    @DisplayName("todaySummary computes the day-over-day revenue delta")
    void todaySummary_computesDelta() {
        LocalDate today = LocalDate.now();
        when(saleRepository.findBetween(eq(pharmacyId), eq(today.atStartOfDay()), any()))
                .thenReturn(List.of(sale(new BigDecimal("100"), today), sale(new BigDecimal("50"), today)));
        when(saleRepository.findBetween(eq(pharmacyId), eq(today.minusDays(1).atStartOfDay()), any()))
                .thenReturn(List.of(sale(new BigDecimal("100"), today.minusDays(1))));

        DailySummaryResponse summary = service.todaySummary(pharmacyId);

        assertEquals(2L, summary.salesCount());
        assertEquals(new BigDecimal("150"), summary.totalRevenue());
        assertEquals(new BigDecimal("100"), summary.previousDayRevenue());
        assertEquals(new BigDecimal("50.00"), summary.revenueDeltaPct());
    }

    @Test
    @DisplayName("todaySummary returns a null delta when yesterday had no revenue")
    void todaySummary_nullDeltaWhenNoBaseline() {
        LocalDate today = LocalDate.now();
        when(saleRepository.findBetween(eq(pharmacyId), eq(today.atStartOfDay()), any()))
                .thenReturn(List.of(sale(new BigDecimal("100"), today)));
        when(saleRepository.findBetween(eq(pharmacyId), eq(today.minusDays(1).atStartOfDay()), any()))
                .thenReturn(List.of());

        DailySummaryResponse summary = service.todaySummary(pharmacyId);

        assertEquals(BigDecimal.ZERO, summary.previousDayRevenue());
        assertNull(summary.revenueDeltaPct());
    }

    @Test
    @DisplayName("timeseries buckets sales/revenue/profit by day and zero-fills the range")
    void timeseries_bucketsByDay() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Sale s1 = sale(new BigDecimal("20"), today);
        Sale s2 = sale(new BigDecimal("5"), yesterday);
        when(saleRepository.findBetween(eq(pharmacyId), any(), any())).thenReturn(List.of(s1, s2));

        // s1: qty 2 @ 10 (cost 6) -> revenue 20, profit 8 ; s2: qty 1 @ 5 (cost 5) -> revenue 5, profit 0
        SaleItem i1 = new SaleItem(UUID.randomUUID(), s1.getId(), UUID.randomUUID(), "A",
                2, new BigDecimal("10"), new BigDecimal("6"));
        SaleItem i2 = new SaleItem(UUID.randomUUID(), s2.getId(), UUID.randomUUID(), "B",
                1, new BigDecimal("5"), new BigDecimal("5"));
        when(saleRepository.findItemsBetween(eq(pharmacyId), any(), any())).thenReturn(List.of(i1, i2));

        List<SalesTimeseriesResponse> series =
                service.timeseries(pharmacyId, yesterday, today, Granularity.DAY);

        assertEquals(2, series.size());
        SalesTimeseriesResponse yBucket = series.get(0);
        SalesTimeseriesResponse tBucket = series.get(1);
        assertEquals(yesterday.toString(), yBucket.period());
        assertEquals(1L, yBucket.salesCount());
        assertEquals(new BigDecimal("5"), yBucket.revenue());
        assertEquals(new BigDecimal("0"), yBucket.profit());
        assertEquals(today.toString(), tBucket.period());
        assertEquals(1L, tBucket.salesCount());
        assertEquals(new BigDecimal("20"), tBucket.revenue());
        assertEquals(new BigDecimal("8"), tBucket.profit());
    }
}
