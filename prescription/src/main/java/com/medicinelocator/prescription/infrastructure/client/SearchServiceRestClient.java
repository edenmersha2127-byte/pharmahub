package com.medicinelocator.prescription.infrastructure.client;

import com.medicinelocator.prescription.application.service.SearchServiceClient;
import com.medicinelocator.prescription.domain.exception.SearchServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SearchServiceRestClient implements SearchServiceClient {

    private static final Logger log = LoggerFactory.getLogger(SearchServiceRestClient.class);

    private final RestTemplate restTemplate;
    private final String searchServiceUrl;
    private final String prescriptionSearchPath;

    public SearchServiceRestClient(
            RestTemplate restTemplate,
            @Value("${search-service.url:http://localhost:8083}") String searchServiceUrl,
            @Value("${search-service.prescription-search-path:/api/search/prescription}")
            String prescriptionSearchPath) {
        this.restTemplate = restTemplate;
        this.searchServiceUrl = searchServiceUrl;
        this.prescriptionSearchPath = prescriptionSearchPath;
    }

    @Override
    public Object searchMedicines(List<String> medicineNames,
                                  Double latitude,
                                  Double longitude,
                                  Double radiusKm) {
        String url = searchServiceUrl + prescriptionSearchPath;


        Double finalLat = (latitude != null) ? latitude : 9.0192;
        Double finalLng = (longitude != null) ? longitude : 38.7525;
        Double finalRadius = (radiusKm != null) ? radiusKm : 10.0;


        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("medicineNames", medicineNames);
        requestBody.put("lat", finalLat);
        requestBody.put("lng", finalLng);
        requestBody.put("radiusKm", finalRadius);
        requestBody.put("page", 0);
        requestBody.put("size", 20);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        headers.set("X-User-Id", "ea83955e-3a7a-481f-a6d9-06857657cd89");
        headers.set("X-User-Role", "CUSTOMER");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        log.info("Calling Search Service: url={} medicineNames={} lat={} lng={} radius={}",
                url, medicineNames, finalLat, finalLng, finalRadius);

        try {
            ResponseEntity<Object> response = restTemplate.postForEntity(url, entity, Object.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Search Service returned non-2xx status: {}", response.getStatusCode());
                throw new SearchServiceUnavailableException(
                        "Search Service returned status: " + response.getStatusCode());
            }

            log.debug("Search Service response received successfully");
            return response.getBody();

        } catch (ResourceAccessException e) {
            log.error("Search Service is unreachable: url={} error={}", url, e.getMessage());
            throw new SearchServiceUnavailableException(
                    "Search Service is unreachable: " + e.getMessage(), e);
        } catch (RestClientException e) {
            log.error("Search Service REST call failed: url={} error={}", url, e.getMessage(), e);
            throw new SearchServiceUnavailableException(
                    "Search Service call failed: " + e.getMessage(), e);
        }
    }
}