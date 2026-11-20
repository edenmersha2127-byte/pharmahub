package com.medicinelocator.salesservice.infrastructure.client;

import com.medicinelocator.salesservice.application.service.InventoryClient;
import com.medicinelocator.salesservice.domain.exception.CheckoutFailedException;
import com.medicinelocator.salesservice.infrastructure.security.GatewayHeaderExtractor;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Calls the inventory-service's atomic batch-deduct endpoint at checkout,
 * forwarding the caller's gateway identity headers so inventory enforces
 * SELL_MEDICINE against the same staff member and rolls the whole cart back on
 * any failure.
 */
@Component
public class InventoryRestClient implements InventoryClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryRestClient.class);

    private static final String[] FORWARDED_HEADERS = {
            GatewayHeaderExtractor.HEADER_USER_ID,
            GatewayHeaderExtractor.HEADER_USER_ROLE,
            GatewayHeaderExtractor.HEADER_USER_EMAIL,
            GatewayHeaderExtractor.HEADER_PHARMACY_ID,
            GatewayHeaderExtractor.HEADER_USER_PERMISSIONS
    };

    private final RestTemplate restTemplate;
    private final String inventoryBaseUrl;

    public InventoryRestClient(
            RestTemplate restTemplate,
            @Value("${inventory-service.url:http://localhost:8082}") String inventoryServiceUrl) {
        this.restTemplate = restTemplate;
        this.inventoryBaseUrl = inventoryServiceUrl;
    }

    @Override
    public List<DeductedItem> deductForSale(UUID pharmacyId, List<Line> items) {
        String url = inventoryBaseUrl + "/api/inventory/pharmacies/" + pharmacyId + "/medicines/deduct-batch";

        List<Map<String, Object>> itemPayload = new ArrayList<>();
        for (Line line : items) {
            itemPayload.add(Map.of("medicineId", line.medicineId(), "quantity", line.quantity()));
        }
        Map<String, Object> body = Map.of("items", itemPayload);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        forwardIdentityHeaders(headers);

        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body, headers),
                    new ParameterizedTypeReference<>() {});

            List<Map<String, Object>> deducted = response.getBody();
            if (deducted == null) {
                throw new CheckoutFailedException("Inventory returned an empty response", false);
            }
            List<DeductedItem> result = new ArrayList<>();
            for (Map<String, Object> m : deducted) {
                result.add(new DeductedItem(
                        UUID.fromString(String.valueOf(m.get("id"))),
                        m.get("medicineName") != null ? String.valueOf(m.get("medicineName")) : null,
                        m.get("price") != null ? new BigDecimal(String.valueOf(m.get("price"))) : BigDecimal.ZERO,
                        m.get("purchasePrice") != null ? new BigDecimal(String.valueOf(m.get("purchasePrice"))) : null));
            }
            return result;
        } catch (HttpClientErrorException e) {
            // 4xx — the caller's fault (insufficient stock, unknown medicine, no permission, no subscription)
            log.warn("Inventory rejected checkout deduction: status={} body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new CheckoutFailedException(
                    "Checkout rejected by inventory: " + e.getResponseBodyAsString(), true);
        } catch (HttpServerErrorException e) {
            log.error("Inventory error during checkout: status={} body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new CheckoutFailedException("Inventory service error during checkout", false);
        } catch (ResourceAccessException e) {
            log.error("Inventory service unreachable during checkout: {}", e.getMessage());
            throw new CheckoutFailedException("Inventory service is unreachable. Please try again.", false);
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
