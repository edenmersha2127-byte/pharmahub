package com.medicinelocator.notificationservice.domain.enums;

/**
 * The full catalogue of events that can produce an in-app notification.
 * Producing services set the type; the frontend can render an icon per type.
 */
public enum NotificationType {

    // ---- Pharmacy admin ----
    REGISTRATION_RECEIVED,      // pharmacy registration submitted, pending approval
    PHARMACY_APPROVED,          // admin approved the pharmacy
    PHARMACY_REJECTED,          // admin rejected the pharmacy (with reason)
    LOW_STOCK,                  // a medicine dropped below the low-stock threshold
    OUT_OF_STOCK,               // a medicine reached zero stock
    MEDICINE_EXPIRING,          // a medicine/batch is expiring soon
    PURCHASE_RECEIVED,          // a purchase order was received into stock
    SALE_COMPLETED,             // a POS sale receipt was completed
    MEDICINE_UPDATE_REMINDER,   // nightly reminder to keep medicines up to date

    // ---- Customer ----
    ACCOUNT_WELCOME,            // account verified / welcome
    PRESCRIPTION_PROCESSED,     // uploaded prescription processed, medicines extracted
    PRESCRIPTION_NEEDS_REVIEW,  // prescription could not be read / needs re-upload
    MEDICINE_BACK_IN_STOCK,     // a searched/wishlisted medicine is available again

    // ---- System admin ----
    NEW_PHARMACY_PENDING,       // a new pharmacy registered and awaits approval
    NEW_REPORT_FLAGGED,         // a pharmacy crossed the report threshold and needs review

    // ---- Reports / moderation ----
    COMPLAINT_RECEIVED,         // a customer filed a report against the pharmacy
    NEW_REPORT_SUBMITTED,       // a new report was filed (per-report alert to report admins)
    PHARMACY_RESPONDED,         // a pharmacy replied to a report (alert to report admins)
    PHARMACY_WARNED,            // admin issued a formal warning (strike) to the pharmacy
    PHARMACY_SUSPENDED,         // admin temporarily suspended the pharmacy
    COMPLAINT_RESOLVED,         // the reporter's complaint was reviewed/closed

    // ---- Subscription / billing ----
    TRIAL_STARTED,              // free trial began for a newly approved pharmacy
    SUBSCRIPTION_ACTIVATED,     // a paid subscription is now active
    SUBSCRIPTION_EXPIRING_SOON, // subscription will expire within the reminder window
    SUBSCRIPTION_EXPIRED,       // subscription lapsed; pharmacy hidden until they renew
    PAYMENT_FAILED,             // a subscription payment could not be confirmed

    // ---- Fallback ----
    GENERAL
}
