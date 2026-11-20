package com.medicinelocator.notificationservice.application.command;

import com.medicinelocator.notificationservice.domain.enums.NotificationType;
import com.medicinelocator.notificationservice.domain.enums.RecipientType;

import java.util.UUID;

public class CreateNotificationCommand {

    private final UUID recipientId;
    private final RecipientType recipientType;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final String link;

    public CreateNotificationCommand(UUID recipientId, RecipientType recipientType,
                                     NotificationType type, String title,
                                     String message, String link) {
        this.recipientId = recipientId;
        this.recipientType = recipientType;
        this.type = type;
        this.title = title;
        this.message = message;
        this.link = link;
    }

    public UUID getRecipientId() { return recipientId; }
    public RecipientType getRecipientType() { return recipientType; }
    public NotificationType getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getLink() { return link; }
}
