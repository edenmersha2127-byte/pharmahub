package com.medicinelocator.notificationservice.application.mapper;

import com.medicinelocator.notificationservice.application.dto.response.ActivityResponse;
import com.medicinelocator.notificationservice.application.dto.response.NotificationResponse;
import com.medicinelocator.notificationservice.domain.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    /** Maps a stored notification to a dashboard activity-feed entry. */
    public ActivityResponse toActivity(Notification notification) {
        return new ActivityResponse(
                notification.getType() != null ? notification.getType().name() : null,
                notification.getTitle(),
                notification.getMessage(),
                null, // no structured amount stored on notifications yet
                notification.getCreatedAt());
    }

    public NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setRecipientId(notification.getRecipientId());
        response.setRecipientType(notification.getRecipientType());
        response.setType(notification.getType());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setLink(notification.getLink());
        response.setRead(notification.isRead());
        response.setCreatedAt(notification.getCreatedAt());
        response.setReadAt(notification.getReadAt());
        return response;
    }
}
