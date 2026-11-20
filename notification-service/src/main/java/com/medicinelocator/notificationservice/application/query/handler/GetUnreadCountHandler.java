package com.medicinelocator.notificationservice.application.query.handler;

import com.medicinelocator.notificationservice.application.dto.response.UnreadCountResponse;
import com.medicinelocator.notificationservice.application.query.GetUnreadCountQuery;
import com.medicinelocator.notificationservice.application.service.NotificationService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetUnreadCountHandler {

    private final NotificationService notificationService;

    public GetUnreadCountHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse handle(GetUnreadCountQuery query) {
        long count = notificationService.countUnread(query.getRecipientId());
        return new UnreadCountResponse(count);
    }
}
