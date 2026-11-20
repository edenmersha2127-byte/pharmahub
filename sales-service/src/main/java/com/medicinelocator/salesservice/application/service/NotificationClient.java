package com.medicinelocator.salesservice.application.service;

import java.util.UUID;

/**
 * Sends in-app notifications to the notification-service. Best-effort:
 * implementations must never throw.
 */
public interface NotificationClient {

    void notify(UUID recipientId, String recipientType, String type,
                String title, String message, String link);
}
