package com.medicinelocator.notificationservice.infrastructure.client;

import com.medicinelocator.notificationservice.application.dto.response.PharmacySummaryResponse;
import com.medicinelocator.notificationservice.application.service.PharmacyDirectoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
public class PharmacyDirectoryRestClient implements PharmacyDirectoryService {

    private static final Logger log = LoggerFactory.getLogger(PharmacyDirectoryRestClient.class);

    private final RestTemplate restTemplate;
    private final String authServiceUrl;

    public PharmacyDirectoryRestClient(
            RestTemplate restTemplate,
            @Value("${auth-service.url:http://localhost:8081}") String authServiceUrl) {
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
    }

    @Override
    public List<PharmacySummaryResponse> findAllPharmacies() {
        String url = authServiceUrl + "/internal/pharmacies";

        log.debug("Fetching pharmacy directory from Auth Service: url={}", url);

        try {
            ResponseEntity<List<PharmacySummaryResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {});

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.debug("Pharmacy directory fetched: count={}", response.getBody().size());
                return response.getBody();
            }

            log.warn("Auth Service returned empty pharmacy directory body");
            return Collections.emptyList();

        } catch (ResourceAccessException e) {
            log.error("Auth Service unreachable while fetching pharmacy directory: url={} error={}",
                    url, e.getMessage());
            return Collections.emptyList();

        } catch (RestClientException e) {
            log.error("Failed to fetch pharmacy directory from Auth Service: error={}",
                    e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
