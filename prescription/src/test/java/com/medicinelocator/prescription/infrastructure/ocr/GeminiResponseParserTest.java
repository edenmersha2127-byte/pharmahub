package com.medicinelocator.prescription.infrastructure.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicinelocator.prescription.domain.enums.ExtractionConfidence;
import com.medicinelocator.prescription.domain.exception.MalformedOcrResponseException;
import com.medicinelocator.prescription.domain.model.ExtractedMedicineCandidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeminiResponseParserTest {

    private GeminiResponseParser parser;

    @BeforeEach
    void setUp() {
        parser = new GeminiResponseParser(new ObjectMapper());
    }

    @Test
    void parse_validJson_returnsCandidates() {
        String json = """
                { "medicines": [
                    { "name": "Amoxicillin", "confidence": "HIGH" },
                    { "name": "Paracetamol", "confidence": "MEDIUM" }
                ] }
                """;

        List<ExtractedMedicineCandidate> result = parser.parse(json);

        assertEquals(2, result.size());
        assertEquals("amoxicillin", result.get(0).getNormalizedName());
        assertEquals(ExtractionConfidence.HIGH, result.get(0).getConfidence());
        assertEquals("paracetamol", result.get(1).getNormalizedName());
        assertEquals(ExtractionConfidence.MEDIUM, result.get(1).getConfidence());
    }

    @Test
    void parse_wrappedInMarkdownFences_stripsAndParses() {
        String json = """
```json
                { "medicines": [ { "name": "Ibuprofen", "confidence": "HIGH" } ] }
```
                """;

        List<ExtractedMedicineCandidate> result = parser.parse(json);

        assertEquals(1, result.size());
        assertEquals("ibuprofen", result.get(0).getNormalizedName());
    }

    @Test
    void parse_emptyMedicinesArray_returnsEmptyList() {
        String json = """
                { "medicines": [] }
                """;

        List<ExtractedMedicineCandidate> result = parser.parse(json);

        assertTrue(result.isEmpty());
    }

    @Test
    void parse_missingMedicinesField_throwsMalformedOcrResponseException() {
        String json = """
                { "something_else": [] }
                """;

        assertThrows(MalformedOcrResponseException.class, () -> parser.parse(json));
    }

    @Test
    void parse_invalidJson_throwsMalformedOcrResponseException() {
        String invalidJson = "this is not json at all {{{";

        assertThrows(MalformedOcrResponseException.class, () -> parser.parse(invalidJson));
    }

    @Test
    void parse_blankInput_returnsEmptyList() {
        List<ExtractedMedicineCandidate> result = parser.parse("");
        assertTrue(result.isEmpty());

        result = parser.parse(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void parse_missingConfidence_defaultsToLow() {
        String json = """
                { "medicines": [ { "name": "Vitamin C" } ] }
                """;

        List<ExtractedMedicineCandidate> result = parser.parse(json);

        assertEquals(1, result.size());
        assertEquals(ExtractionConfidence.LOW, result.get(0).getConfidence());
    }

    @Test
    void parse_duplicateNames_deduplicates() {
        String json = """
                { "medicines": [
                    { "name": "Amoxicillin", "confidence": "HIGH" },
                    { "name": "amoxicillin", "confidence": "MEDIUM" },
                    { "name": "AMOXICILLIN", "confidence": "LOW" }
                ] }
                """;

        List<ExtractedMedicineCandidate> result = parser.parse(json);

        assertEquals(1, result.size());
    }

    @Test
    void parse_blankNameEntries_areSkipped() {
        String json = """
                { "medicines": [
                    { "name": "", "confidence": "HIGH" },
                    { "name": "Paracetamol", "confidence": "HIGH" },
                    { "name": "   ", "confidence": "LOW" }
                ] }
                """;

        List<ExtractedMedicineCandidate> result = parser.parse(json);

        assertEquals(1, result.size());
        assertEquals("paracetamol", result.get(0).getNormalizedName());
    }
}