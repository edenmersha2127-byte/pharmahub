package com.medicinelocator.prescription.infrastructure.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicinelocator.prescription.domain.exception.MalformedOcrResponseException;
import com.medicinelocator.prescription.domain.exception.OcrProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;


@Component
public class GeminiVisionProvider implements OcrProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiVisionProvider.class);
    private static final String PROVIDER_NAME = "gemini_vision";

    private static final String EXTRACTION_PROMPT = """
            You are a medical prescription extraction assistant.
            Analyze the prescription image and identify all medicines prescribed.
            
            Rules:
            - Extract only medicine names.
            - Ignore doctor information, hospital information, signatures, stamps, addresses, phone numbers, dates, and patient details.
            - If a medicine name is unclear, include the best interpretation and mark confidence as LOW.
            - Return valid JSON only.
            - Do not return explanations or markdown.
            
            Response format:
            { "medicines": [ { "name": "Medicine Name", "confidence": "HIGH" } ] }
            """;

    private final String apiKey;
    private final String model;
    private final String endpoint;
    private final int maxRetries;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiVisionProvider(
            @Value("${ocr.gemini.api-key}") String apiKey,
            @Value("${ocr.gemini.model:gemini-2.0-flash}") String model,
            @Value("${ocr.gemini.endpoint:https://generativelanguage.googleapis.com/v1beta/models}")
            String endpoint,
            @Value("${ocr.gemini.max-retries:2}") int maxRetries,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.endpoint = endpoint;
        this.maxRetries = maxRetries;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }


    @Override
    public String extractText(MultipartFile file) {
        log.debug("GeminiVisionProvider: processing file={} size={}",
                file.getOriginalFilename(), file.getSize());

        byte[] imageBytes;
        String mimeType;
        try {
            imageBytes = file.getBytes();
            mimeType = resolveMimeType(file);
        } catch (Exception e) {
            throw new OcrProcessingException("Failed to read uploaded file bytes", e);
        }

        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String requestBody = buildRequestBody(base64Image, mimeType);
        String url = endpoint + "/" + model + ":generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        return callWithRetry(url, entity, 0);
    }

    private String callWithRetry(String url, HttpEntity<String> entity, int attempt) {
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new OcrProcessingException(
                        "Gemini Vision API returned non-2xx status: " + response.getStatusCode());
            }

            return extractGeminiText(response.getBody());

        } catch (HttpClientErrorException e) {

            log.error("Gemini API client error: status={} body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new OcrProcessingException(
                    "Gemini Vision API rejected the request: " + e.getStatusCode()
                            + " — " + e.getResponseBodyAsString(), e);

        } catch (HttpServerErrorException | ResourceAccessException e) {

            if (attempt < maxRetries) {
                log.warn("Gemini API call failed (attempt {}/{}), retrying: {}",
                        attempt + 1, maxRetries, e.getMessage());
                return callWithRetry(url, entity, attempt + 1);
            }
            log.error("Gemini API unreachable after {} retries: {}", maxRetries, e.getMessage());
            throw new OcrProcessingException(
                    "Gemini Vision API is unavailable after " + maxRetries + " retries: "
                            + e.getMessage(), e);

        } catch (RestClientException e) {
            throw new OcrProcessingException("Gemini Vision API call failed: " + e.getMessage(), e);
        }
    }

    private String buildRequestBody(String base64Image, String mimeType) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", EXTRACTION_PROMPT),
                                            Map.of("inline_data", Map.of(
                                                    "mime_type", mimeType,
                                                    "data", base64Image
                                            ))
                                    )
                            )
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.1,
                            "responseMimeType", "application/json"
                    )
            );
            return objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            throw new OcrProcessingException("Failed to build Gemini Vision API request body", e);
        }
    }


    private String extractGeminiText(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode error = root.path("error");
            if (!error.isMissingNode()) {
                String message = error.path("message").asText("Unknown Gemini API error");
                throw new OcrProcessingException("Gemini API error: " + message);
            }

            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                log.warn("Gemini API returned no candidates. Raw response: {}", responseBody);
                throw new MalformedOcrResponseException(
                        "Gemini API returned no candidates in response");
            }

            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                log.warn("Gemini API returned no content parts. Raw response: {}", responseBody);
                throw new MalformedOcrResponseException(
                        "Gemini API returned no content parts in response");
            }

            String text = parts.get(0).path("text").asText("");
            if (text.isBlank()) {
                log.warn("Gemini API returned blank text. Raw response: {}", responseBody);
                throw new MalformedOcrResponseException("Gemini API returned blank extraction text");
            }

            log.debug("Gemini raw extraction text: {}", text);
            return text;

        } catch (OcrProcessingException | MalformedOcrResponseException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse Gemini API response envelope. Raw response: {}", responseBody, e);
            throw new MalformedOcrResponseException(
                    "Failed to parse Gemini API response envelope", e);
        }
    }

    private String resolveMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            return "image/jpeg";
        }
        return contentType;
    }
}