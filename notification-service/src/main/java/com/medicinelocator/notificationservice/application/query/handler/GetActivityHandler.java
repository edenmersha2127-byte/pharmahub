package com.medicinelocator.notificationservice.application.query.handler;

import com.medicinelocator.notificationservice.application.dto.response.ActivityResponse;
import com.medicinelocator.notificationservice.application.mapper.NotificationMapper;
import com.medicinelocator.notificationservice.application.query.GetActivityQuery;
import com.medicinelocator.notificationservice.application.service.NotificationService;
import com.medicinelocator.notificationservice.domain.enums.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Component
public class GetActivityHandler {

    /** Notification types that make up the pharmacy dashboard's activity feed. */
    private static final Set<NotificationType> ACTIVITY_TYPES = Set.of(
            NotificationType.SALE_COMPLETED,
            NotificationType.PURCHASE_RECEIVED,
            NotificationType.LOW_STOCK,
            NotificationType.MEDICINE_EXPIRING);

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    public GetActivityHandler(NotificationService notificationService,
                              NotificationMapper notificationMapper) {
        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> handle(GetActivityQuery query) {
        return notificationService
                .findRecentActivity(query.getRecipientId(), ACTIVITY_TYPES, query.getLimit())
                .stream()
                .map(notificationMapper::toActivity)
                .toList();
    }
}
