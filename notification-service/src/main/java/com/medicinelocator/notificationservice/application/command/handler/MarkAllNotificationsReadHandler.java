package com.medicinelocator.notificationservice.application.command.handler;

import com.medicinelocator.notificationservice.application.command.MarkAllNotificationsReadCommand;
import com.medicinelocator.notificationservice.application.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MarkAllNotificationsReadHandler {

    private static final Logger log = LoggerFactory.getLogger(MarkAllNotificationsReadHandler.class);

    private final NotificationService notificationService;

    public MarkAllNotificationsReadHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Transactional
    public int handle(MarkAllNotificationsReadCommand command) {
        int updated = notificationService.markAllAsRead(command.getRecipientId());
        log.info("Marked all notifications read: recipientId={} count={}",
                command.getRecipientId(), updated);
        return updated;
    }
}
