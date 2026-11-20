package com.medicinelocator.paymentservice.infrastructure.client;

import com.medicinelocator.paymentservice.application.service.PharmacyAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Calls the Auth Service internal endpoints to look up pharmacies and push
 * subscription state.
 */
@Component
public class AuthAccessRestClient implements PharmacyAccessService {

    private static final Logger log = LoggerFactory.getLogger(AuthAccessRestClient.class);

    private final RestTemplate restTemplate;
    private final String pharmaciesUrl;

    public AuthAccessRestClient(
            RestTemplate restTemplate,
            @Value("${auth-service.url:http://localhost:8081}") String authServiceUrl) {
        this.restTemplate = restTemplate;
        this.pharmaciesUrl = authServiceUrl + "/internal/pharmacies";
    }

    @Override
    public Optional<PharmacyInfo> getPharmacy(UUID pharmacyId) {
        try {
            ResponseEntity<PharmacyLookupResponse> response = restTemplate.getForEntity(
                    pharmaciesUrl + "/" + pharmacyId, PharmacyLookupResponse.class);
            PharmacyLookupResponse body = response.getBody();
            if (body == null) {
                return Optional.empty();
            }
            return Optional.of(new PharmacyInfo(body.pharmacyId, body.pharmacyName, body.email));
        } catch (ResourceAccessException e) {
            log.error("Auth unreachable while fetching pharmacy {}: {}", pharmacyId, e.getMessage());
            return Optional.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch pharmacy {}: {}", pharmacyId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void pushSubscriptionState(UUID pharmacyId, String status, LocalDateTime subscribedUntil) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(pharmaciesUrl + "/" + pharmacyId + "/subscription")
                .queryParam("status", status);
        if (subscribedUntil != null) {
            builder.queryParam("until", subscribedUntil.toString());
        }
        String url = builder.toUriString();
        try {
            restTemplate.exchange(url, HttpMethod.POST, null, Void.class);
            log.info("Pushed subscription state to auth: pharmacyId={} status={} until={}",
                    pharmacyId, status, subscribedUntil);
        } catch (ResourceAccessException e) {
            log.error("Auth unreachable pushing subscription state for {}: {}", pharmacyId, e.getMessage());
        } catch (RestClientException e) {
            log.error("Failed to push subscription state for {}: {}", pharmacyId, e.getMessage());
        }
    }

    /** Binds the subset of auth's pharmacy payload we need. */
    private static class PharmacyLookupResponse {
        public UUID pharmacyId;
        public String pharmacyName;
        public String email;
    }
}
