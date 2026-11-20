package com.medicinelocator.inventory.infrastructure.client;

import com.medicinelocator.inventory.application.dto.response.PharmacyProfileResponse;
import com.medicinelocator.inventory.application.service.PharmacyProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

/**
 * The ONE AND ONLY implementation of PharmacyProfileService.
 * Fetches pharmacy profile from the Auth Service via REST.
 *
 * Endpoint: GET {authServiceUrl}/internal/pharmacies/{pharmacyId}
 */
@Component
public class AuthServiceRestClient implements PharmacyProfileService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceRestClient.class);

    private final RestTemplate restTemplate;
    private final String authServiceUrl;

    public AuthServiceRestClient(
            RestTemplate restTemplate,
            @Value("${auth-service.url:http://auth-service:8081}") String authServiceUrl) {
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
    }

    @Override
    public Optional<PharmacyProfileResponse> findById(UUID pharmacyId) {
        String url = authServiceUrl + "/internal/pharmacies/" + pharmacyId;

        log.debug("Fetching pharmacy profile from Auth Service: pharmacyId={} url={}",
                pharmacyId, url);

        try {
            ResponseEntity<PharmacyProfileResponse> response = restTemplate.getForEntity(
                    url, PharmacyProfileResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.debug("Pharmacy profile fetched: pharmacyId={} name={}",
                        pharmacyId, response.getBody().getPharmacyName());
                return Optional.of(response.getBody());
            }

            log.warn("Auth Service returned empty body for pharmacyId={}", pharmacyId);
            return Optional.empty();

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Pharmacy not found in Auth Service: pharmacyId={}", pharmacyId);
            return Optional.empty();

        } catch (ResourceAccessException e) {
            log.error("Auth Service unreachable: url={} error={}", url, e.getMessage());
            throw new RuntimeException(
                    "Auth Service is unreachable while fetching pharmacy profile for pharmacyId="
                            + pharmacyId, e);

        } catch (RestClientException e) {
            log.error("Auth Service call failed: pharmacyId={} error={}",
                    pharmacyId, e.getMessage(), e);
            throw new RuntimeException(
                    "Failed to fetch pharmacy profile from Auth Service for pharmacyId="
                            + pharmacyId + ": " + e.getMessage(), e);
        }
    }
}