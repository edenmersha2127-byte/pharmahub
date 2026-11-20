package com.medicinelocator.notificationservice.application.dto.response;

/**
 * Drives the number on the notification bell badge.
 */
public class UnreadCountResponse {

    private long unreadCount;

    public UnreadCountResponse() {
    }

    public UnreadCountResponse(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }
}
