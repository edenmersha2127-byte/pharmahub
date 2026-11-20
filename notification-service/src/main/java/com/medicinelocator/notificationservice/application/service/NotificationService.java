package com.medicinelocator.notificationservice.application.service;

import com.medicinelocator.notificationservice.domain.enums.NotificationType;
import com.medicinelocator.notificationservice.domain.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application-layer persistence port for notifications.
 * Implemented by the infrastructure persistence adapter.
 */
public interface NotificationService {

    Notification save(Notification notification);

    Optional<Notification> findByIdAndRecipientId(UUID id, UUID recipientId);

    Page<Notification> findByRecipient(UUID recipientId, boolean unreadOnly, Pageable pageable);

    /** The most-recent events of the given types for a recipient, newest first, capped at {@code limit}. */
    List<Notification> findRecentActivity(UUID recipientId, Collection<NotificationType> types, int limit);

    long countUnread(UUID recipientId);

    /**
     * Marks every unread notification for the recipient as read.
     *
     * @return the number of notifications that were updated
     */
    int markAllAsRead(UUID recipientId);
}
