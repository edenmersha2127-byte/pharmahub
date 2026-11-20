package com.medicinelocator.notificationservice.application.command;

import java.util.UUID;

public class MarkNotificationReadCommand {

    private final UUID notificationId;
    private final UUID recipientId;

    public MarkNotificationReadCommand(UUID notificationId, UUID recipientId) {
        this.notificationId = notificationId;
        this.recipientId = recipientId;
    }

    public UUID getNotificationId() { return notificationId; }
    public UUID getRecipientId() { return recipientId; }
}
