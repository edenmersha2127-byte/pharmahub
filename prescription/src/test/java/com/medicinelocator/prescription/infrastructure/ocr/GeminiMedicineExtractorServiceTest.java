package com.medicinelocator.prescription.infrastructure.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeminiMedicineExtractorServiceTest {

    private GeminiMedicineExtractorService extractorService;

    @BeforeEach
    void setUp() {
        GeminiResponseParser parser = new GeminiResponseParser(new ObjectMapper());
        extractorService = new GeminiMedicineExtractorService(parser);
    }

    @Test
    void extract_validGeminiJson_returnsNormalizedNames() {
        String geminiJson = """
                { "medicines": [
                    { "name": "Amoxicillin 500mg", "confidence": "HIGH" },
                    { "name": "Paracetamol", "confidence": "MEDIUM" }
                ] }
                """;

        List<String> result = extractorService.extract(geminiJson);

        assertEquals(2, result.size());
        assertTrue(result.contains("amoxicillin mg"));
        assertTrue(result.contains("paracetamol"));
    }

    @Test
    void extract_emptyInput_returnsEmptyList() {
        assertTrue(extractorService.extract("").isEmpty());
        assertTrue(extractorService.extract(null).isEmpty());
    }

    @Test
    void extract_noMedicinesFound_returnsEmptyList() {
        String json = """
                { "medicines": [] }
                """;

        List<String> result = extractorService.extract(json);

        assertTrue(result.isEmpty());
    }
}