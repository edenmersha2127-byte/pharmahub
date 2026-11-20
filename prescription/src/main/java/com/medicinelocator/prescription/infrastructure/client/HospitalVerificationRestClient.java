package com.medicinelocator.prescription.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicinelocator.prescription.application.service.HospitalVerificationClient;
import com.medicinelocator.prescription.domain.model.HospitalConnection;
import com.medicinelocator.prescription.domain.model.VerificationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Calls an integrated hospital's public verification endpoint
 * ({@code GET {baseUrl}/api/verify?prescriptionId=...}) with the shared API key.
 * Fails closed: any error → UNVERIFIABLE (never "valid").
 */
@Component
public class HospitalVerificationRestClient implements HospitalVerificationClient {

    private static final Logger log = LoggerFactory.getLogger(HospitalVerificationRestClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String verifyPath;
    private final String apiKeyHeader;

    public HospitalVerificationRestClient(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${hospital-verify.path:/api/verify}") String verifyPath,
            @Value("${hospital-verify.api-key-header:X-Api-Key}") String apiKeyHeader) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.verifyPath = verifyPath;
        this.apiKeyHeader = apiKeyHeader;
    }

    @Override
    public VerificationOutcome verify(HospitalConnection connection, String prescriptionId) {
        String baseUrl = trimTrailingSlash(connection.getBaseUrl());
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + verifyPath)
                .queryParam("prescriptionId", prescriptionId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        if (connection.getApiKey() != null && !connection.getApiKey().isBlank()) {
            headers.set(apiKeyHeader, connection.getApiKey());
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        log.info("Verifying prescription: hospitalId={} prescriptionId={} url={}",
                connection.getHospitalId(), prescriptionId, url);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Verification: non-2xx/empty from hospital {}: {}",
                        connection.getHospitalId(), response.getStatusCode());
                return VerificationOutcome.unverifiable(connection.getHospitalId(), prescriptionId,
                        "status " + response.getStatusCode());
            }

            JsonNode body = objectMapper.readTree(response.getBody());
            boolean valid = body.path("valid").asBoolean(false);

            return valid
                    ? VerificationOutcome.valid(connection.getHospitalId(), prescriptionId)
                    : VerificationOutcome.invalid(connection.getHospitalId(), prescriptionId);

        } catch (RestClientException e) {
            log.warn("Verification call failed for hospital {}: {}",
                    connection.getHospitalId(), e.getMessage());
            return VerificationOutcome.unverifiable(connection.getHospitalId(), prescriptionId, e.getMessage());
        } catch (Exception e) {
            log.warn("Verification response parse failed for hospital {}: {}",
                    connection.getHospitalId(), e.getMessage());
            return VerificationOutcome.unverifiable(connection.getHospitalId(), prescriptionId, e.getMessage());
        }
    }

    private String trimTrailingSlash(String url) {
        if (url == null) {
            return "";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
