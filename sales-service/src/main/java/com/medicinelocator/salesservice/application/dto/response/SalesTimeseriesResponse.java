package com.medicinelocator.salesservice.application.dto.response;

import java.math.BigDecimal;

/**
 * One time bucket of the sales trend: number of sales, revenue (Σ line totals)
 * and profit (Σ line (sell − buy)). {@code period} is "YYYY-MM-DD" for DAY
 * granularity or "YYYY-MM" for MONTH. Buckets are zero-filled and continuous.
 */
public record SalesTimeseriesResponse(
        String period,
        long salesCount,
        BigDecimal revenue,
        BigDecimal profit
) {
}
