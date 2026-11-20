package com.medicinelocator.notificationservice.application.command.handler;

import com.medicinelocator.notificationservice.application.command.MarkNotificationReadCommand;
import com.medicinelocator.notificationservice.application.service.NotificationService;
import com.medicinelocator.notificationservice.domain.exception.NotificationNotFoundException;
import com.medicinelocator.notificationservice.domain.model.Notification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MarkNotificationReadHandler {

    private final NotificationService notificationService;

    public MarkNotificationReadHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Transactional
    public Notification handle(MarkNotificationReadCommand command) {
                Notification notification = notificationService
                .findByIdAndRecipientId(command.getNotificationId(), command.getRecipientId())
                .orElseThrow(() -> new NotificationNotFoundException(command.getNotificationId()));

        notification.markAsRead();
        return notificationService.save(notification);
    }
}
