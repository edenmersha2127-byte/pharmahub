package com.medicinelocator.prescription.infrastructure.ocr;

import com.medicinelocator.prescription.domain.enums.ExtractionConfidence;
import com.medicinelocator.prescription.domain.model.ExtractedMedicineCandidate;
import com.medicinelocator.prescription.infrastructure.matching.FuzzyMedicineMatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FuzzyMedicineMatchingServiceTest {

    private FuzzyMedicineMatchingService matchingService;

    @BeforeEach
    void setUp() {
        matchingService = new FuzzyMedicineMatchingService(0.75, 5);
    }

    @Test
    void findClosestMatches_misspelledParacetamol_findsCorrectMatch() {
        List<String> known = List.of("Paracetamol", "Ibuprofen", "Amoxicillin");

        List<String> matches = matchingService.findClosestMatches("Paracetmol", known);

        assertFalse(matches.isEmpty());
        assertEquals("Paracetamol", matches.get(0));
    }

    @Test
    void findClosestMatches_misspelledAmoxicillin_findsCorrectMatch() {
        List<String> known = List.of("Paracetamol", "Ibuprofen", "Amoxicillin");

        List<String> matches = matchingService.findClosestMatches("Amoxcillin", known);

        assertFalse(matches.isEmpty());
        assertEquals("Amoxicillin", matches.get(0));
    }

    @Test
    void findClosestMatches_exactMatch_caseInsensitive() {
        List<String> known = List.of("Paracetamol", "Ibuprofen");

        List<String> matches = matchingService.findClosestMatches("ibuprofen", known);

        assertFalse(matches.isEmpty());
        assertEquals("Ibuprofen", matches.get(0));
    }

    @Test
    void findClosestMatches_completelyUnrelatedName_returnsEmpty() {
        List<String> known = List.of("Paracetamol", "Ibuprofen", "Amoxicillin");

        List<String> matches = matchingService.findClosestMatches("xyzqqqzzz123", known);

        assertTrue(matches.isEmpty());
    }

    @Test
    void findClosestMatches_emptyInput_returnsEmpty() {
        List<String> known = List.of("Paracetamol");

        assertTrue(matchingService.findClosestMatches("", known).isEmpty());
        assertTrue(matchingService.findClosestMatches(null, known).isEmpty());
        assertTrue(matchingService.findClosestMatches("Paracetamol", List.of()).isEmpty());
    }

    @Test
    void correctMedicineNames_mixedConfidenceCandidates_correctsAll() {
        List<ExtractedMedicineCandidate> candidates = List.of(
                new ExtractedMedicineCandidate("Paracetmol", "paracetmol", ExtractionConfidence.LOW),
                new ExtractedMedicineCandidate("Ibuprofen", "ibuprofen", ExtractionConfidence.HIGH)
        );
        List<String> known = List.of("Paracetamol", "Ibuprofen", "Amoxicillin");

        List<String> corrected = matchingService.correctMedicineNames(candidates, known);

        assertEquals(2, corrected.size());
        assertEquals("Paracetamol", corrected.get(0));
        assertEquals("Ibuprofen", corrected.get(1));
    }

    @Test
    void correctMedicineNames_noMatchFound_keepsOriginalNormalized() {
        List<ExtractedMedicineCandidate> candidates = List.of(
                new ExtractedMedicineCandidate("RandomUnknownDrug", "randomunknowndrug", ExtractionConfidence.LOW)
        );
        List<String> known = List.of("Paracetamol", "Ibuprofen");

        List<String> corrected = matchingService.correctMedicineNames(candidates, known);

        assertEquals(1, corrected.size());
        assertEquals("randomunknowndrug", corrected.get(0));
    }
}