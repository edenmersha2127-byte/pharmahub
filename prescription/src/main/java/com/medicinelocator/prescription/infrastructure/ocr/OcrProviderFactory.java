package com.medicinelocator.prescription.infrastructure.ocr;

import com.medicinelocator.prescription.application.service.MedicineExtractorService;
import com.medicinelocator.prescription.application.service.OcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class OcrProviderFactory {

    private static final Logger log = LoggerFactory.getLogger(OcrProviderFactory.class);

    @Bean
    @Primary
    public OcrService activeOcrService(
            @Value("${ocr.provider:gemini_vision}") String providerName,
            List<OcrService> availableProviders) {

        Map<String, OcrService> providerMap = availableProviders.stream()
                .filter(p -> p instanceof OcrProvider)
                .collect(Collectors.toMap(
                        p -> ((OcrProvider) p).getProviderName().toLowerCase(),
                        Function.identity()
                ));

        OcrService selected = providerMap.get(providerName.toLowerCase());

        if (selected == null) {
            log.warn(
                    "OCR provider '{}' not found. Available: {}. Falling back.",
                    providerName,
                    providerMap.keySet()
            );

            selected = providerMap.values().stream()
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalStateException("No OCR providers registered"));
        }

        log.info("Active OCR provider: {}", providerName);

        return selected;
    }

    @Bean
    @Primary
    public MedicineExtractorService activeMedicineExtractorService(
            @Value("${ocr.provider:gemini_vision}") String providerName,
            GeminiMedicineExtractorService geminiMedicineExtractorService,
            OcrResultParser ocrResultParser) {

        if ("gemini_vision".equalsIgnoreCase(providerName)) {
            log.info(
                    "Active medicine extractor: GeminiMedicineExtractorService (JSON-based)"
            );
            return geminiMedicineExtractorService;
        }

        log.info(
                "Active medicine extractor: OcrResultParser (regex-based, provider={})",
                providerName
        );

        return ocrResultParser;
    }
}