package com.medicinelocator.reportservice.domain.enums;

/**
 * Type of software/experience issue a user (customer, pharmacy, staff or admin)
 * reports to the platform admins via a support ticket. Distinct from
 * {@link ReportCategory}, which classifies a complaint against a pharmacy.
 */
public enum IssueCategory {
    SEARCH_NOT_WORKING,
    UPLOAD_FAILED,
    PAYMENT_ISSUE,
    LOGIN_PROBLEM,
    BUG,
    FEATURE_REQUEST,
    OTHER
}
