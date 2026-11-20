package com.medicinelocator.prescription.application.service;

import java.util.UUID;

/**
 * Sends in-app notifications to the Notification Service.
 *
 * Best-effort contract: implementations must never throw. A notification
 * failure must not break prescription processing.
 */
public interface NotificationClient {

    /**
     * Create a single in-app notification for one recipient.
     *
     * @param recipientId   the recipient's account id (here: the uploading customer id)
     * @param recipientType one of CUSTOMER, PHARMACY, ADMIN
     * @param type          the NotificationType name (e.g. PRESCRIPTION_PROCESSED)
     * @param title         short headline
     * @param message       body text
     * @param link          in-app link to open when clicked (may be null)
     */
    void notify(UUID recipientId, String recipientType, String type,
                String title, String message, String link);
}
