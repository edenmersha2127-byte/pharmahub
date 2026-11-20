package com.medicinelocator.notificationservice.application.command;

import java.util.UUID;

public class MarkAllNotificationsReadCommand {

    private final UUID recipientId;

    public MarkAllNotificationsReadCommand(UUID recipientId) {
        this.recipientId = recipientId;
    }

    public UUID getRecipientId() { return recipientId; }
}
