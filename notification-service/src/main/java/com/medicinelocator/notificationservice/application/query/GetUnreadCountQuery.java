package com.medicinelocator.notificationservice.application.query;

import java.util.UUID;

public class GetUnreadCountQuery {

    private final UUID recipientId;

    public GetUnreadCountQuery(UUID recipientId) {
        this.recipientId = recipientId;
    }

    public UUID getRecipientId() { return recipientId; }
}
