package com.medicinelocator.inventory.infrastructure.client;

import com.medicinelocator.inventory.application.service.SubscriptionAccessChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Checks a pharmacy's subscription access via the Auth Service.
 *
 * Endpoint: GET {auth-service.url}/internal/pharmacies/{id}/subscription
 *
 * Fail-open on connectivity errors (allow, log) so an Auth outage doesn't block
 * legitimate pharmacies; only an explicit "inactive" response blocks the action.
 */
@Component
public class SubscriptionAccessRestClient implements SubscriptionAccessChecker {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionAccessRestClient.class);

    private final RestTemplate restTemplate;
    private final String authServiceUrl;

    public SubscriptionAccessRestClient(
            RestTemplate restTemplate,
            @Value("${auth-service.url:http://auth-service:8081}") String authServiceUrl) {
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
    }

    @Override
    public boolean hasActiveSubscription(UUID pharmacyId) {
        String url = authServiceUrl + "/internal/pharmacies/" + pharmacyId + "/subscription";
        try {
            ResponseEntity<AccessResponse> response =
                    restTemplate.getForEntity(url, AccessResponse.class);
            AccessResponse body = response.getBody();
            return body != null && body.active;
        } catch (ResourceAccessException e) {
            log.error("Auth unreachable for subscription check {} — allowing (fail-open): {}",
                    pharmacyId, e.getMessage());
            return true;
        } catch (RestClientException e) {
            log.error("Subscription check failed for {} — allowing (fail-open): {}",
                    pharmacyId, e.getMessage());
            return true;
        }
    }

    /** Binds the subset of auth's subscription-access payload we need. */
    private static class AccessResponse {
        public boolean active;
    }
}
