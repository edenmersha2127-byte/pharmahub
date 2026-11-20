package com.medicinelocator.notificationservice.application.command.handler;

import com.medicinelocator.notificationservice.application.command.CreateNotificationCommand;
import com.medicinelocator.notificationservice.application.service.NotificationService;
import com.medicinelocator.notificationservice.domain.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateNotificationHandler {

    private static final Logger log = LoggerFactory.getLogger(CreateNotificationHandler.class);

    private final NotificationService notificationService;

    public CreateNotificationHandler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Transactional
    public Notification handle(CreateNotificationCommand command) {
        Notification notification = Notification.create(
                command.getRecipientId(),
                command.getRecipientType(),
                command.getType(),
                command.getTitle(),
                command.getMessage(),
                command.getLink()
        );

        Notification saved = notificationService.save(notification);

        log.info("Notification created: id={} type={} recipientId={} recipientType={}",
                saved.getId(), saved.getType(), saved.getRecipientId(), saved.getRecipientType());
        return saved;
    }
}
