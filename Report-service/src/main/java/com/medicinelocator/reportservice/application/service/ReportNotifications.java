package com.medicinelocator.reportservice.application.service;

/**
 * Notification type + recipient-type string constants sent to the Notification
 * Service. The type values must exactly match the NotificationType enum names
 * declared in the notification-service.
 */
public final class ReportNotifications {

    private ReportNotifications() {
    }

    // recipient types (match notification-service RecipientType)
    public static final String CUSTOMER = "CUSTOMER";
    public static final String PHARMACY = "PHARMACY";
    public static final String ADMIN = "ADMIN";

    // notification types (must exist in notification-service NotificationType)
    public static final String COMPLAINT_RECEIVED = "COMPLAINT_RECEIVED";
    public static final String NEW_REPORT_SUBMITTED = "NEW_REPORT_SUBMITTED";
    public static final String PHARMACY_RESPONDED = "PHARMACY_RESPONDED";
    public static final String NEW_REPORT_FLAGGED = "NEW_REPORT_FLAGGED";
    public static final String PHARMACY_WARNED = "PHARMACY_WARNED";
    public static final String PHARMACY_SUSPENDED = "PHARMACY_SUSPENDED";
    public static final String COMPLAINT_RESOLVED = "COMPLAINT_RESOLVED";
    public static final String GENERAL = "GENERAL";

    // frontend deep-links
    public static final String LINK_PHARMACY_REPORTS = "/pharmacy/reports";
    public static final String LINK_ADMIN_REPORTS = "/admin/reports";
    public static final String LINK_MY_REPORTS = "/my-reports";
}
