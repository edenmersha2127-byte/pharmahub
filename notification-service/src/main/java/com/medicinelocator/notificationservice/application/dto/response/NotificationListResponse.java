package com.medicinelocator.notificationservice.application.dto.response;

import java.util.List;

/**
 * A page of notifications for the bell dropdown, plus the current unread count
 * so the frontend can render the badge from the same response.
 */
public class NotificationListResponse {

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private long unreadCount;
    private List<NotificationResponse> notifications;

    public NotificationListResponse() {
    }

    public NotificationListResponse(int page, int size, long totalElements, int totalPages,
                                    long unreadCount, List<NotificationResponse> notifications) {
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.unreadCount = unreadCount;
        this.notifications = notifications;
    }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }

    public List<NotificationResponse> getNotifications() { return notifications; }
    public void setNotifications(List<NotificationResponse> notifications) {
        this.notifications = notifications;
    }
}
