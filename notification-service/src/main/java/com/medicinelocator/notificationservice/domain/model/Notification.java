package com.medicinelocator.notificationservice.domain.model;

import com.medicinelocator.notificationservice.domain.enums.NotificationType;
import com.medicinelocator.notificationservice.domain.enums.RecipientType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A single in-app notification shown behind the website's notification bell.
 *
 * Pure domain object: no framework annotations. Persistence and transport
 * mapping live in the infrastructure and application layers respectively.
 */
public class Notification {

    private UUID id;
    private UUID recipientId;
    private RecipientType recipientType;
    private NotificationType type;
    private String title;
    private String message;
    private String link;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public Notification() {
    }

    public Notification(UUID id, UUID recipientId, RecipientType recipientType,
                        NotificationType type, String title, String message, String link,
                        boolean read, LocalDateTime createdAt, LocalDateTime readAt) {
        this.id = id;
        this.recipientId = recipientId;
        this.recipientType = recipientType;
        this.type = type;
        this.title = title;
        this.message = message;
        this.link = link;
        this.read = read;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    /**
     * Factory for a brand-new, unread notification.
     */
    public static Notification create(UUID recipientId, RecipientType recipientType,
                                      NotificationType type, String title,
                                      String message, String link) {
        Notification n = new Notification();
        n.recipientId = recipientId;
        n.recipientType = recipientType;
        n.type = type;
        n.title = title;
        n.message = message;
        n.link = link;
        n.read = false;
        return n;
    }

    /**
     * Domain behavior: mark this notification as read. Idempotent.
     */
    public void markAsRead() {
        if (!this.read) {
            this.read = true;
            this.readAt = LocalDateTime.now();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getRecipientId() { return recipientId; }
    public void setRecipientId(UUID recipientId) { this.recipientId = recipientId; }

    public RecipientType getRecipientType() { return recipientType; }
    public void setRecipientType(RecipientType recipientType) { this.recipientType = recipientType; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
