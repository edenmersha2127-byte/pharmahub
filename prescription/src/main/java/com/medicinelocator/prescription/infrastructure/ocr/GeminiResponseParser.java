package com.medicinelocator.prescription.infrastructure.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicinelocator.prescription.domain.enums.ExtractionConfidence;
import com.medicinelocator.prescription.domain.exception.MalformedOcrResponseException;
import com.medicinelocator.prescription.domain.model.ExtractedMedicineCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


@Component
public class GeminiResponseParser {

    private static final Logger log = LoggerFactory.getLogger(GeminiResponseParser.class);

    private final ObjectMapper objectMapper;

    public GeminiResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<ExtractedMedicineCandidate> parse(String rawGeminiText) {
        if (rawGeminiText == null || rawGeminiText.isBlank()) {
            log.warn("GeminiResponseParser: received blank input");
            return List.of();
        }

        String cleaned = stripMarkdownFences(rawGeminiText.trim());

        JsonNode root;
        try {
            root = objectMapper.readTree(cleaned);
        } catch (Exception e) {
            log.error("GeminiResponseParser: failed to parse JSON. Raw text: {}", rawGeminiText, e);
            throw new MalformedOcrResponseException(
                    "Gemini response is not valid JSON: " + e.getMessage(), e);
        }

        JsonNode medicines = root.path("medicines");
        if (!medicines.isArray()) {
            log.warn("GeminiResponseParser: 'medicines' field is missing or not an array. Raw text: {}",
                    rawGeminiText);
            throw new MalformedOcrResponseException(
                    "Gemini response missing required 'medicines' array field");
        }

        List<ExtractedMedicineCandidate> candidates = new ArrayList<>();
        Set<String> seenNormalized = new LinkedHashSet<>();

        for (JsonNode medicineNode : medicines) {
            String rawName = medicineNode.path("name").asText("").trim();
            if (rawName.isBlank()) {
                continue;
            }

            String normalized = normalize(rawName);
            if (normalized.isBlank() || seenNormalized.contains(normalized)) {
                continue;
            }
            seenNormalized.add(normalized);

            ExtractionConfidence confidence = parseConfidence(
                    medicineNode.path("confidence").asText("LOW"));

            candidates.add(new ExtractedMedicineCandidate(rawName, normalized, confidence));
        }

        log.debug("GeminiResponseParser: parsed {} medicine candidates from response", candidates.size());
        return candidates;
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

    private String normalize(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private ExtractionConfidence parseConfidence(String value) {
        try {
            return ExtractionConfidence.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            return ExtractionConfidence.LOW;
        }
    }
}