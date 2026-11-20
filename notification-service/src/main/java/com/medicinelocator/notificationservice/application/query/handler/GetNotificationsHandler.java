package com.medicinelocator.notificationservice.application.query.handler;

import com.medicinelocator.notificationservice.application.dto.response.NotificationListResponse;
import com.medicinelocator.notificationservice.application.dto.response.NotificationResponse;
import com.medicinelocator.notificationservice.application.mapper.NotificationMapper;
import com.medicinelocator.notificationservice.application.query.GetNotificationsQuery;
import com.medicinelocator.notificationservice.application.service.NotificationService;
import com.medicinelocator.notificationservice.domain.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class GetNotificationsHandler {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    public GetNotificationsHandler(NotificationService notificationService,
                                   NotificationMapper notificationMapper) {
        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
    }

    @Transactional(readOnly = true)
    public NotificationListResponse handle(GetNotificationsQuery query) {
        PageRequest pageable = PageRequest.of(
                query.getPage(), query.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> page = notificationService.findByRecipient(
                query.getRecipientId(), query.isUnreadOnly(), pageable);

        List<NotificationResponse> notifications = page.getContent()
                .stream()
                .map(notificationMapper::toResponse)
                .toList();

        long unreadCount = notificationService.countUnread(query.getRecipientId());

        return new NotificationListResponse(
                query.getPage(),
                query.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                unreadCount,
                notifications
        );
    }
}
