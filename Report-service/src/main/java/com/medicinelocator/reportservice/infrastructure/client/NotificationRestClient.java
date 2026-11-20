package com.medicinelocator.reportservice.infrastructure.client;

import com.medicinelocator.reportservice.application.service.NotificationNotifier;
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
 * Posts in-app notifications to the Notification Service's internal endpoint.
 *
 * Target: POST {notification-service.url}/internal/notifications
 *
 * Best-effort: unreachable service or non-2xx responses are logged and
 * swallowed so the caller's business transaction is never affected.
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
            log.debug("Notification sent: type={} recipientId={} recipientType={}",
                    type, recipientId, recipientType);
        } catch (ResourceAccessException e) {
            log.error("Notification Service unreachable: url={} type={} error={}",
                    notificationUrl, type, e.getMessage());
        } catch (RestClientException e) {
            log.error("Failed to send notification: type={} recipientId={} error={}",
                    type, recipientId, e.getMessage());
        }
    }
}
