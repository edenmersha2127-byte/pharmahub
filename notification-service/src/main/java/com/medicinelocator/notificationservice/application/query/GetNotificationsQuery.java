package com.medicinelocator.notificationservice.application.query;

import java.util.UUID;

public class GetNotificationsQuery {

    private final UUID recipientId;
    private final boolean unreadOnly;
    private final int page;
    private final int size;

    public GetNotificationsQuery(UUID recipientId, boolean unreadOnly, int page, int size) {
        this.recipientId = recipientId;
        this.unreadOnly = unreadOnly;
        this.page = page;
        this.size = size;
    }

    public UUID getRecipientId() { return recipientId; }
    public boolean isUnreadOnly() { return unreadOnly; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}
