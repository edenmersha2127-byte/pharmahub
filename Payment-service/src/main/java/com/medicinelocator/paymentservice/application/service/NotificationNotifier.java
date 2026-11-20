package com.medicinelocator.paymentservice.application.service;

import java.util.UUID;

/**
 * Sends in-app notifications to the Notification Service. Best-effort: never throws.
 */
public interface NotificationNotifier {

    void notify(UUID recipientId, String recipientType, String type,
                String title, String message, String link);
}
