package com.medicinelocator.prescription.infrastructure.ocr;

import com.medicinelocator.prescription.application.service.MedicineExtractorService;
import com.medicinelocator.prescription.domain.model.ExtractedMedicineCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class GeminiMedicineExtractorService implements MedicineExtractorService {

    private static final Logger log = LoggerFactory.getLogger(GeminiMedicineExtractorService.class);

    private final GeminiResponseParser geminiResponseParser;

    public GeminiMedicineExtractorService(GeminiResponseParser geminiResponseParser) {
        this.geminiResponseParser = geminiResponseParser;
    }

    @Override
    public List<String> extract(String rawOcrText) {
        if (rawOcrText == null || rawOcrText.isBlank()) {
            log.debug("GeminiMedicineExtractorService: empty input");
            return List.of();
        }

        List<ExtractedMedicineCandidate> candidates = geminiResponseParser.parse(rawOcrText);

        List<String> names = candidates.stream()
                .map(ExtractedMedicineCandidate::getNormalizedName)
                .filter(name -> !name.isBlank())
                .distinct()
                .toList();

        log.info("GeminiMedicineExtractorService: extracted {} medicine names: {}",
                names.size(), names);
        return names;
    }


    public List<ExtractedMedicineCandidate> extractWithConfidence(String rawOcrText) {
        if (rawOcrText == null || rawOcrText.isBlank()) {
            return List.of();
        }
        return geminiResponseParser.parse(rawOcrText);
    }
}