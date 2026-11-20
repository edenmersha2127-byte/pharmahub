package com.medicinelocator.reportservice.infrastructure.client;

import com.medicinelocator.reportservice.application.dto.response.PharmacySummaryResponse;
import com.medicinelocator.reportservice.application.service.PharmacyModerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Calls the Auth Service internal endpoints to read pharmacy summaries and to
 * apply moderation actions (suspend / strike / lift) that mutate pharmacy state
 * owned by Auth.
 *
 * Reads are best-effort (empty on failure). Mutating actions rethrow on failure
 * so an admin is never told an action succeeded when it did not.
 */
@Component
public class AuthModerationRestClient implements PharmacyModerationService {

    private static final Logger log = LoggerFactory.getLogger(AuthModerationRestClient.class);

    private final RestTemplate restTemplate;
    private final String pharmaciesUrl;
    private final String reportAdminsUrl;

    public AuthModerationRestClient(
            RestTemplate restTemplate,
            @Value("${auth-service.url:http://localhost:8081}") String authServiceUrl) {
        this.restTemplate = restTemplate;
        this.pharmaciesUrl = authServiceUrl + "/internal/pharmacies";
        this.reportAdminsUrl = authServiceUrl + "/internal/admins?role=REPORT_ADMIN";
    }

    @Override
    public Optional<PharmacySummaryResponse> getPharmacy(UUID pharmacyId) {
        String url = pharmaciesUrl + "/" + pharmacyId;
        try {
            ResponseEntity<PharmacySummaryResponse> response =
                    restTemplate.getForEntity(url, PharmacySummaryResponse.class);
            return Optional.ofNullable(response.getBody());
        } catch (ResourceAccessException e) {
            log.error("Auth Service unreachable while fetching pharmacy {}: {}", pharmacyId, e.getMessage());
            return Optional.empty();
        } catch (RestClientException e) {
            log.error("Failed to fetch pharmacy {} from Auth Service: {}", pharmacyId, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<UUID> getReportAdminIds() {
        try {
            ResponseEntity<List<UUID>> response = restTemplate.exchange(
                    reportAdminsUrl, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
            List<UUID> body = response.getBody();
            return body != null ? body : Collections.emptyList();
        } catch (ResourceAccessException e) {
            log.error("Auth Service unreachable while fetching report-admin ids: {}", e.getMessage());
            return Collections.emptyList();
        } catch (RestClientException e) {
            log.error("Failed to fetch report-admin ids from Auth Service: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public void suspend(UUID pharmacyId, int days) {
        String url = UriComponentsBuilder
                .fromHttpUrl(pharmaciesUrl + "/" + pharmacyId + "/suspend")
                .queryParam("days", days)
                .toUriString();
        postOrThrow(url, "suspend", pharmacyId);
    }

    @Override
    public void addStrike(UUID pharmacyId) {
        postOrThrow(pharmaciesUrl + "/" + pharmacyId + "/strike", "strike", pharmacyId);
    }

    @Override
    public void liftSuspension(UUID pharmacyId) {
        postOrThrow(pharmaciesUrl + "/" + pharmacyId + "/lift-suspension", "lift-suspension", pharmacyId);
    }

    private void postOrThrow(String url, String action, UUID pharmacyId) {
        try {
            ResponseEntity<Void> response =
                    restTemplate.exchange(url, HttpMethod.POST, null, Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException(
                        "Auth Service returned " + response.getStatusCode() + " for " + action);
            }
            log.info("Moderation action '{}' applied on pharmacy {}", action, pharmacyId);
        } catch (ResourceAccessException e) {
            log.error("Auth Service unreachable for {} on pharmacy {}: {}", action, pharmacyId, e.getMessage());
            throw new IllegalStateException("Auth Service is unreachable; " + action + " could not be applied.", e);
        } catch (RestClientException e) {
            log.error("{} call failed on pharmacy {}: {}", action, pharmacyId, e.getMessage());
            throw new IllegalStateException(action + " could not be applied: " + e.getMessage(), e);
        }
    }
}
