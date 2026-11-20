package com.medicinelocator.notificationservice.presentation.controller;

import com.medicinelocator.notificationservice.application.command.CreateNotificationCommand;
import com.medicinelocator.notificationservice.application.command.handler.CreateNotificationHandler;
import com.medicinelocator.notificationservice.application.dto.request.CreateNotificationRequest;
import com.medicinelocator.notificationservice.application.dto.response.NotificationResponse;
import com.medicinelocator.notificationservice.application.mapper.NotificationMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal REST endpoint consumed by other services (auth, inventory,
 * prescription, ...) to create in-app notifications.
 *
 * Route: POST /internal/notifications
 *
 * This endpoint is NOT exposed through the public API Gateway.
 * It is a service-to-service endpoint only. In production, restrict access
 * to this path at the network/gateway level.
 */
@RestController
@RequestMapping("/internal/notifications")
public class NotificationInternalController {

    private static final Logger log = LoggerFactory.getLogger(NotificationInternalController.class);

    private final CreateNotificationHandler createNotificationHandler;
    private final NotificationMapper notificationMapper;

    public NotificationInternalController(CreateNotificationHandler createNotificationHandler,
                                          NotificationMapper notificationMapper) {
        this.createNotificationHandler = createNotificationHandler;
        this.notificationMapper = notificationMapper;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {

        log.debug("Internal create-notification request: type={} recipientId={}",
                request.getType(), request.getRecipientId());

        NotificationResponse response = notificationMapper.toResponse(
                createNotificationHandler.handle(new CreateNotificationCommand(
                        request.getRecipientId(),
                        request.getRecipientType(),
                        request.getType(),
                        request.getTitle(),
                        request.getMessage(),
                        request.getLink())));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
