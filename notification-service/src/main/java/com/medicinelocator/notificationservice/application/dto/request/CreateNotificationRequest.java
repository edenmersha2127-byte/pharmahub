package com.medicinelocator.notificationservice.application.dto.request;

import com.medicinelocator.notificationservice.domain.enums.NotificationType;
import com.medicinelocator.notificationservice.domain.enums.RecipientType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Payload posted by other services (auth, inventory, prescription, ...) to
 * create an in-app notification for a single recipient.
 */
public class CreateNotificationRequest {

    @NotNull
    private UUID recipientId;

    @NotNull
    private RecipientType recipientType;

    @NotNull
    private NotificationType type;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    private String link;

    public CreateNotificationRequest() {
    }

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
}
