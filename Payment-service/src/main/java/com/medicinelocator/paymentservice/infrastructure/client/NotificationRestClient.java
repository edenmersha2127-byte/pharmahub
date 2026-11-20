package com.medicinelocator.paymentservice.infrastructure.client;

import com.medicinelocator.paymentservice.application.service.NotificationNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Posts in-app notifications to the Notification Service. Best-effort.
 */
@Component
public class NotificationRestClient implements NotificationNotifier {

    private static final Logger log = LoggerFactory.getLogger(NotificationRestClient.class);

    private final RestTemplate restTemplate;
    private final String notificationUrl;

    public NotificationRestClient(
            RestTemplate restTemplate,
            @Value("${notification-service.url:http://localhost:8086}") String notificationServiceUrl) {
        this.restTemplate = restTemplate;
        this.notificationUrl = notificationServiceUrl + "/internal/notifications";
    }

    @Override
    public void notify(UUID recipientId, String recipientType, String type,
                       String title, String message, String link) {
        if (recipientId == null) {
            log.warn("Skipping notification with null recipientId: type={}", type);
            return;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("recipientId", recipientId);
        body.put("recipientType", recipientType);
        body.put("type", type);
        body.put("title", title);
        body.put("message", message);
        body.put("link", link);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            restTemplate.postForEntity(notificationUrl, new HttpEntity<>(body, headers), Void.class);
            log.debug("Notification sent: type={} recipientId={}", type, recipientId);
        } catch (ResourceAccessException e) {
            log.error("Notification Service unreachable: type={} error={}", type, e.getMessage());
        } catch (RestClientException e) {
            log.error("Failed to send notification: type={} error={}", type, e.getMessage());
        }
    }
}
