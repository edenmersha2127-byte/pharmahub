package com.medicinelocator.notificationservice.application.query;

import java.util.UUID;

public class GetActivityQuery {

    private final UUID recipientId;
    private final int limit;

    public GetActivityQuery(UUID recipientId, int limit) {
        this.recipientId = recipientId;
        this.limit = limit;
    }

    public UUID getRecipientId() { return recipientId; }
    public int getLimit() { return limit; }
}
