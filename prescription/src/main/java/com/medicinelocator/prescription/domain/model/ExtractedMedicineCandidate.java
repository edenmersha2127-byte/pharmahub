package com.medicinelocator.prescription.domain.model;

import com.medicinelocator.prescription.domain.enums.ExtractionConfidence;

/**
 * Value object representing one medicine candidate extracted by an OCR/vision
 * provider, before persistence. Carries a confidence label so downstream
 * fuzzy-matching logic can prioritize HIGH-confidence extractions.
 *
 * No Spring annotations — pure domain.
 */
public class ExtractedMedicineCandidate {

    private final String rawName;
    private final String normalizedName;
    private final ExtractionConfidence confidence;

    public ExtractedMedicineCandidate(String rawName, String normalizedName,
                                      ExtractionConfidence confidence) {
        this.rawName = rawName;
        this.normalizedName = normalizedName;
        this.confidence = confidence;
    }

    public String getRawName() { return rawName; }
    public String getNormalizedName() { return normalizedName; }
    public ExtractionConfidence getConfidence() { return confidence; }
}