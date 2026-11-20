package com.medicinelocator.notificationservice.presentation.controller;

import com.medicinelocator.notificationservice.application.command.MarkAllNotificationsReadCommand;
import com.medicinelocator.notificationservice.application.command.MarkNotificationReadCommand;
import com.medicinelocator.notificationservice.application.command.handler.MarkAllNotificationsReadHandler;
import com.medicinelocator.notificationservice.application.command.handler.MarkNotificationReadHandler;
import com.medicinelocator.notificationservice.application.dto.response.ActivityResponse;
import com.medicinelocator.notificationservice.application.dto.response.NotificationListResponse;
import com.medicinelocator.notificationservice.application.dto.response.NotificationResponse;
import com.medicinelocator.notificationservice.application.dto.response.UnreadCountResponse;
import com.medicinelocator.notificationservice.application.mapper.NotificationMapper;
import com.medicinelocator.notificationservice.application.query.GetActivityQuery;
import com.medicinelocator.notificationservice.application.query.GetNotificationsQuery;
import com.medicinelocator.notificationservice.application.query.GetUnreadCountQuery;
import com.medicinelocator.notificationservice.application.query.handler.GetActivityHandler;
import com.medicinelocator.notificationservice.application.query.handler.GetNotificationsHandler;
import com.medicinelocator.notificationservice.application.query.handler.GetUnreadCountHandler;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Customer / pharmacy facing notification API (behind the gateway's JwtAuth
 * filter, which injects the X-User-Id header identifying the recipient).
 */
@RestController
@RequestMapping("/api/notifications")
@Validated
public class NotificationController {

    private final GetNotificationsHandler getNotificationsHandler;
    private final GetUnreadCountHandler getUnreadCountHandler;
    private final MarkNotificationReadHandler markNotificationReadHandler;
    private final MarkAllNotificationsReadHandler markAllNotificationsReadHandler;
    private final GetActivityHandler getActivityHandler;
    private final NotificationMapper notificationMapper;

    public NotificationController(GetNotificationsHandler getNotificationsHandler,
                                 GetUnreadCountHandler getUnreadCountHandler,
                                 MarkNotificationReadHandler markNotificationReadHandler,
                                 MarkAllNotificationsReadHandler markAllNotificationsReadHandler,
                                 GetActivityHandler getActivityHandler,
                                 NotificationMapper notificationMapper) {
        this.getNotificationsHandler = getNotificationsHandler;
        this.getUnreadCountHandler = getUnreadCountHandler;
        this.markNotificationReadHandler = markNotificationReadHandler;
        this.markAllNotificationsReadHandler = markAllNotificationsReadHandler;
        this.getActivityHandler = getActivityHandler;
        this.notificationMapper = notificationMapper;
    }

    /** The bell dropdown list. */
    @GetMapping
    public ResponseEntity<NotificationListResponse> getNotifications(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        NotificationListResponse response = getNotificationsHandler.handle(
                new GetNotificationsQuery(parseUserId(userId), unreadOnly, page, size));
        return ResponseEntity.ok(response);
    }

    /** The dashboard "Recent Activities" feed: recent sales, purchases, and stock alerts. */
    @GetMapping("/activity")
    public ResponseEntity<List<ActivityResponse>> getActivity(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {

        List<ActivityResponse> activity = getActivityHandler.handle(
                new GetActivityQuery(parseUserId(userId), limit));
        return ResponseEntity.ok(activity);
    }

    /** The badge number on the bell. */
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @RequestHeader("X-User-Id") String userId) {

        UnreadCountResponse response = getUnreadCountHandler.handle(
                new GetUnreadCountQuery(parseUserId(userId)));
        return ResponseEntity.ok(response);
    }

    /** Mark a single notification as read. */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID id) {

        NotificationResponse response = notificationMapper.toResponse(
                markNotificationReadHandler.handle(
                        new MarkNotificationReadCommand(id, parseUserId(userId))));
        return ResponseEntity.ok(response);
    }

    /** Mark all of the recipient's notifications as read. */
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(
            @RequestHeader("X-User-Id") String userId) {

        int updated = markAllNotificationsReadHandler.handle(
                new MarkAllNotificationsReadCommand(parseUserId(userId)));
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    private UUID parseUserId(String userId) {
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid or missing X-User-Id header");
        }
    }
}
