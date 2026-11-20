package com.medicinelocator.prescription.infrastructure.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicinelocator.prescription.application.service.PrescriptionIdentityExtractor;
import com.medicinelocator.prescription.domain.model.PrescriptionIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * A SEPARATE Gemini Vision call that reads only the hospital ID + prescription ID.
 * It does not touch the medicine-extraction prompt or parser in any way.
 *
 * <p>Fails "soft": on any error it returns an empty identity, which the
 * verification step treats as unidentifiable (fail closed) rather than crashing
 * the upload.
 */
@Component
public class GeminiPrescriptionIdentityExtractor implements PrescriptionIdentityExtractor {

    private static final Logger log = LoggerFactory.getLogger(GeminiPrescriptionIdentityExtractor.class);

    private static final String IDENTITY_PROMPT = """
            You are a prescription identity reader.
            From the prescription image, read these two printed identifiers if present:
            - hospitalId: the hospital's ID / code (examples: "HSP-001", "WASH-7F3K")
            - prescriptionId: the prescription's unique ID (examples: "RX-2026-000001", "RX-7K9MF-2QX8P")

            Return ONLY valid JSON. No markdown, no explanations.
            Response format:
            { "hospitalId": "", "prescriptionId": "" }

            Use an empty string for any value that is not clearly present.
            """;

    private final String apiKey;
    private final String model;
    private final String endpoint;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiPrescriptionIdentityExtractor(
            @Value("${ocr.gemini.api-key:}") String apiKey,
            @Value("${ocr.gemini.model:gemini-2.0-flash}") String model,
            @Value("${ocr.gemini.endpoint:https://generativelanguage.googleapis.com/v1beta/models}")
            String endpoint,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.endpoint = endpoint;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public PrescriptionIdentity extractIdentity(MultipartFile file) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            String mimeType = (file.getContentType() != null && !file.getContentType().isBlank())
                    ? file.getContentType() : "image/jpeg";

            String requestBody = buildRequestBody(base64Image, mimeType);
            String url = endpoint + "/" + model + ":generateContent?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Identity extraction: Gemini returned non-2xx or empty body: {}",
                        response.getStatusCode());
                return PrescriptionIdentity.empty();
            }

            String text = extractGeminiText(response.getBody());
            return parseIdentity(text);

        } catch (Exception e) {
            log.warn("Identity extraction failed (treating as unidentifiable): {}", e.getMessage());
            return PrescriptionIdentity.empty();
        }
    }

    private String buildRequestBody(String base64Image, String mimeType) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", IDENTITY_PROMPT),
                                Map.of("inline_data", Map.of(
                                        "mime_type", mimeType,
                                        "data", base64Image
                                ))
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.0,
                        "responseMimeType", "application/json"
                )
        );
        return objectMapper.writeValueAsString(requestBody);
    }

    private String extractGeminiText(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            return "";
        }
        return parts.get(0).path("text").asText("");
    }

    private PrescriptionIdentity parseIdentity(String text) {
        if (text == null || text.isBlank()) {
            return PrescriptionIdentity.empty();
        }
        String cleaned = stripMarkdownFences(text.trim());
        try {
            JsonNode node = objectMapper.readTree(cleaned);
            String hospitalId = node.path("hospitalId").asText("").trim();
            String prescriptionId = node.path("prescriptionId").asText("").trim();
            log.debug("Extracted identity: hospitalId='{}' prescriptionId='{}'", hospitalId, prescriptionId);
            return new PrescriptionIdentity(
                    hospitalId.isBlank() ? null : hospitalId,
                    prescriptionId.isBlank() ? null : prescriptionId);
        } catch (Exception e) {
            log.warn("Identity extraction: could not parse JSON '{}': {}", cleaned, e.getMessage());
            return PrescriptionIdentity.empty();
        }
    }

    private String stripMarkdownFences(String text) {
        String result = text;
        if (result.startsWith("```json")) {
            result = result.substring(7);
        } else if (result.startsWith("```")) {
            result = result.substring(3);
        }
        if (result.endsWith("```")) {
            result = result.substring(0, result.length() - 3);
        }
        return result.trim();
    }
}
