package com.medicinelocator.prescription.infrastructure.matching;

import com.medicinelocator.prescription.domain.enums.ExtractionConfidence;
import com.medicinelocator.prescription.domain.model.ExtractedMedicineCandidate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FuzzyMedicineMatchingService")
class FuzzyMedicineMatchingServiceTest {

    private FuzzyMedicineMatchingService service;
    private List<String> knownMedicines;

    @BeforeEach
    void setUp() {
        service = new FuzzyMedicineMatchingService(0.75, 5);
        knownMedicines = List.of(
                "Paracetamol", "Amoxicillin", "Ibuprofen", "Aspirin",
                "Metformin", "Atorvastatin", "Omeprazole"
        );
    }

    @Test
    @DisplayName("returns empty list when input or known list is null/empty")
    void findClosestMatches_handlesEmptyInputs() {
        assertTrue(service.findClosestMatches(null, knownMedicines).isEmpty());
        assertTrue(service.findClosestMatches("", knownMedicines).isEmpty());
        assertTrue(service.findClosestMatches("Paracetamol", null).isEmpty());
        assertTrue(service.findClosestMatches("Paracetamol", List.of()).isEmpty());
    }

    @Test
    @DisplayName("finds exact match (case-insensitive)")
    void findClosestMatches_findsExactCaseInsensitive() {
        List<String> matches = service.findClosestMatches("ibuprofen", knownMedicines);
        assertFalse(matches.isEmpty());
        assertEquals("Ibuprofen", matches.get(0));
    }

    @Test
    @DisplayName("corrects a typo: 'Paracetmol' → 'Paracetamol'")
    void findClosestMatches_correctsTypo() {
        List<String> matches = service.findClosestMatches("Paracetmol", knownMedicines);
        assertFalse(matches.isEmpty());
        assertEquals("Paracetamol", matches.get(0));
    }

    @Test
    @DisplayName("corrects another typo: 'Amoxcillin' → 'Amoxicillin'")
    void findClosestMatches_correctsAnotherTypo() {
        List<String> matches = service.findClosestMatches("Amoxcillin", knownMedicines);
        assertFalse(matches.isEmpty());
        assertEquals("Amoxicillin", matches.get(0));
    }

    @Test
    @DisplayName("returns empty when no candidate exceeds similarity threshold")
    void findClosestMatches_returnsEmptyForCompletelyDifferent() {
        List<String> matches = service.findClosestMatches("Xyzabcdefg", knownMedicines);
        assertTrue(matches.isEmpty());
    }

    @Test
    @DisplayName("respects maxCandidatesPerMedicine limit")
    void findClosestMatches_respectsLimit() {
        FuzzyMedicineMatchingService limited = new FuzzyMedicineMatchingService(0.0, 2);
        List<String> matches = limited.findClosestMatches("ibuprofen", knownMedicines);
        assertTrue(matches.size() <= 2);
    }

    @Test
    @DisplayName("correctMedicineNames(): falls back to normalized name when no match found")
    void correctMedicineNames_fallsBackToNormalized() {
        ExtractedMedicineCandidate candidate = new ExtractedMedicineCandidate(
                "Xyzabcdefg", "xyzabcdefg", ExtractionConfidence.LOW);

        List<String> result = service.correctMedicineNames(List.of(candidate), knownMedicines);

        assertEquals(1, result.size());
        assertEquals("xyzabcdefg", result.get(0));
    }

    @Test
    @DisplayName("correctMedicineNames(): picks corrected name for typos")
    void correctMedicineNames_correctsTypos() {
        ExtractedMedicineCandidate c1 = new ExtractedMedicineCandidate(
                "Paracetmol", "paracetmol", ExtractionConfidence.HIGH);
        ExtractedMedicineCandidate c2 = new ExtractedMedicineCandidate(
                "Aspirin", "aspirin", ExtractionConfidence.HIGH);

        List<String> result = service.correctMedicineNames(List.of(c1, c2), knownMedicines);

        assertEquals(2, result.size());
        assertEquals("Paracetamol", result.get(0));
        assertEquals("Aspirin", result.get(1));
    }
}
