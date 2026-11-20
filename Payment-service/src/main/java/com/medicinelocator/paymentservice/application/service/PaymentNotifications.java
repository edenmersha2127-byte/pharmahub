package com.medicinelocator.paymentservice.application.service;

/**
 * Notification type + recipient constants sent to the Notification Service.
 * Type values must match the notification-service NotificationType enum.
 */
public final class PaymentNotifications {

    private PaymentNotifications() {
    }

    public static final String PHARMACY = "PHARMACY";

    public static final String TRIAL_STARTED = "TRIAL_STARTED";
    public static final String SUBSCRIPTION_ACTIVATED = "SUBSCRIPTION_ACTIVATED";
    public static final String SUBSCRIPTION_EXPIRING_SOON = "SUBSCRIPTION_EXPIRING_SOON";
    public static final String SUBSCRIPTION_EXPIRED = "SUBSCRIPTION_EXPIRED";
    public static final String PAYMENT_FAILED = "PAYMENT_FAILED";

    public static final String LINK_SUBSCRIPTION = "/pharmacy/subscription";
}
