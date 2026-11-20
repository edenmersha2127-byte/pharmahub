package com.medicinelocator.prescription.infrastructure.client;

import com.medicinelocator.prescription.application.service.InventoryLookupClient;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Calls inventory-service's pharmacy-scoped medicine lookup, forwarding the
 * caller's gateway identity headers so inventory enforces the pharmacy's own
 * access rules (VIEW_INVENTORY). Best-effort: any failure yields no matches so
 * the dispense scan still returns the extracted names for manual handling.
 */
@Component
public class InventoryLookupRestClient implements InventoryLookupClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryLookupRestClient.class);

    private static final String[] FORWARDED_HEADERS = {
            "X-User-Id", "X-User-Role", "X-User-Email", "X-Pharmacy-Id", "X-User-Permissions"
    };

    private final RestTemplate restTemplate;
    private final String inventoryBaseUrl;

    public InventoryLookupRestClient(
            RestTemplate restTemplate,
            @Value("${inventory-service.url:http://localhost:8082}") String inventoryBaseUrl) {
        this.restTemplate = restTemplate;
        this.inventoryBaseUrl = inventoryBaseUrl;
    }

    @Override
    public List<Medicine> lookup(UUID pharmacyId, String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String url = UriComponentsBuilder
                .fromHttpUrl(inventoryBaseUrl)
                .path("/api/inventory/pharmacies/{pharmacyId}/medicines/lookup")
                .queryParam("q", query)
                .buildAndExpand(pharmacyId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        forwardIdentityHeaders(headers);

        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {});
            List<Map<String, Object>> body = response.getBody();
            if (body == null || body.isEmpty()) {
                return List.of();
            }
            List<Medicine> result = new ArrayList<>(body.size());
            for (Map<String, Object> m : body) {
                result.add(new Medicine(
                        m.get("id") != null ? UUID.fromString(String.valueOf(m.get("id"))) : null,
                        m.get("medicineName") != null ? String.valueOf(m.get("medicineName")) : null,
                        m.get("price") != null ? new BigDecimal(String.valueOf(m.get("price"))) : null,
                        m.get("stockQuantity") != null
                                ? Integer.parseInt(String.valueOf(m.get("stockQuantity"))) : 0,
                        Boolean.parseBoolean(String.valueOf(m.get("requiresPrescription")))));
            }
            return result;
        } catch (RestClientException e) {
            log.warn("Inventory lookup failed (non-fatal): pharmacyId={} query='{}' error={}",
                    pharmacyId, query, e.getMessage());
            return List.of();
        }
    }

    private void forwardIdentityHeaders(HttpHeaders headers) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        for (String name : FORWARDED_HEADERS) {
            String value = request.getHeader(name);
            if (value != null) {
                headers.set(name, value);
            }
        }
    }
}
