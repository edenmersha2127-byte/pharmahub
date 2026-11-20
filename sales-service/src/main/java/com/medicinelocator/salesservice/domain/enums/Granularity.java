package com.medicinelocator.salesservice.domain.enums;

/**
 * Bucket size for the sales time-series analytics endpoint.
 */
public enum Granularity {
    /** One bucket per calendar day (period key "YYYY-MM-DD"). */
    DAY,
    /** One bucket per calendar month (period key "YYYY-MM"). */
    MONTH
}
