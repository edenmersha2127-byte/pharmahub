package com.medicinelocator.paymentservice.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicinelocator.paymentservice.application.service.PaymentGateway;
import com.medicinelocator.paymentservice.domain.exception.PaymentProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Chapa gateway integration (https://developer.chapa.co).
 *
 *  - initialize: POST {base}/v1/transaction/initialize  (Bearer secret key)
 *  - verify:     GET  {base}/v1/transaction/verify/{tx_ref}
 *
 * We always verify server-side before activating a subscription; the browser
 * redirect / webhook is only a trigger, never the source of truth.
 */
@Component
public class ChapaClient implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(ChapaClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String secretKey;
    private final String callbackUrl;
    private final String returnUrl;
    private final String currency;

    public ChapaClient(RestTemplate restTemplate,
                       ObjectMapper objectMapper,
                       @Value("${chapa.base-url:https://api.chapa.co}") String baseUrl,
                       @Value("${chapa.secret-key:}") String secretKey,
                       @Value("${chapa.callback-url:}") String callbackUrl,
                       @Value("${chapa.return-url:}") String returnUrl,
                       @Value("${chapa.currency:ETB}") String currency) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.secretKey = secretKey;
        this.callbackUrl = callbackUrl;
        this.returnUrl = returnUrl;
        this.currency = currency;
    }

    @Override
    public String initializeCheckout(CheckoutRequest request) {
        String url = baseUrl + "/v1/transaction/initialize";

        Map<String, Object> body = new HashMap<>();
        body.put("amount", request.amount().toPlainString());
        body.put("currency", currency);
        body.put("email", request.email());
        body.put("first_name", request.firstName());
        body.put("last_name", request.lastName());
        body.put("tx_ref", request.txRef());
        body.put("callback_url", callbackUrl);
        body.put("return_url", returnUrl);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body, authHeaders()), String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            if (!response.getStatusCode().is2xxSuccessful()
                    || !"success".equalsIgnoreCase(text(root, "status"))) {
                throw new PaymentProcessingException(
                        "Chapa initialize failed: " + text(root, "message"));
            }
            String checkoutUrl = root.path("data").path("checkout_url").asText(null);
            if (checkoutUrl == null || checkoutUrl.isBlank()) {
                throw new PaymentProcessingException("Chapa did not return a checkout URL.");
            }
            log.info("Chapa checkout initialized: txRef={}", request.txRef());
            return checkoutUrl;

        } catch (ResourceAccessException e) {
            throw new PaymentProcessingException("Chapa is unreachable.", e);
        } catch (RestClientException | com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new PaymentProcessingException("Chapa initialize error: " + e.getMessage(), e);
        }
    }

    @Override
    public VerificationResult verify(String txRef) {
        String url = baseUrl + "/v1/transaction/verify/" + txRef;
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(authHeaders()), String.class);

            String raw = response.getBody();
            JsonNode root = objectMapper.readTree(raw);
            // Chapa returns status:"success" at the top and data.status:"success" when paid.
            boolean topOk = "success".equalsIgnoreCase(text(root, "status"));
            String dataStatus = root.path("data").path("status").asText("");
            boolean paid = topOk && "success".equalsIgnoreCase(dataStatus);
            log.info("Chapa verify: txRef={} paid={} dataStatus={}", txRef, paid, dataStatus);
            return new VerificationResult(paid, dataStatus, raw);

        } catch (ResourceAccessException e) {
            throw new PaymentProcessingException("Chapa is unreachable during verify.", e);
        } catch (RestClientException | com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new PaymentProcessingException("Chapa verify error: " + e.getMessage(), e);
        }
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(secretKey);
        return headers;
    }

    private String text(JsonNode node, String field) {
        return node.path(field).asText("");
    }
}
