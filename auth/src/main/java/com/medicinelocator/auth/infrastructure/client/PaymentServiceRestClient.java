package com.medicinelocator.auth.infrastructure.client;

import com.medicinelocator.auth.application.service.SubscriptionTrialNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

/**
 * Calls the Payment Service to start a pharmacy's free trial on approval.
 * Best-effort: failures are logged, never thrown (approval must still succeed).
 */
@Component
public class PaymentServiceRestClient implements SubscriptionTrialNotifier {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceRestClient.class);

    private final RestTemplate restTemplate;
    private final String trialUrlBase;

    public PaymentServiceRestClient(
            RestTemplate restTemplate,
            @Value("${payment-service.url:http://localhost:8088}") String paymentServiceUrl) {
        this.restTemplate = restTemplate;
        this.trialUrlBase = paymentServiceUrl + "/internal/subscriptions/trial/";
    }

    @Override
    public void startTrial(UUID pharmacyId) {
        String url = trialUrlBase + pharmacyId;
        try {
            restTemplate.exchange(url, HttpMethod.POST, null, Void.class);
            log.info("Requested trial start for pharmacy {}", pharmacyId);
        } catch (ResourceAccessException e) {
            log.error("Payment Service unreachable to start trial for {}: {}", pharmacyId, e.getMessage());
        } catch (RestClientException e) {
            log.error("Failed to start trial for {}: {}", pharmacyId, e.getMessage());
        }
    }
}
