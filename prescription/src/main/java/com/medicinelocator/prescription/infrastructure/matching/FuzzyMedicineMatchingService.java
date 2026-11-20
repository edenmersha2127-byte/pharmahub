package com.medicinelocator.prescription.infrastructure.matching;

import com.medicinelocator.prescription.application.service.MedicineMatchingService;
import com.medicinelocator.prescription.domain.model.ExtractedMedicineCandidate;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Fuzzy matching implementation using a combination of Jaro-Winkler similarity
 * (good for catching transposed/dropped characters like "Paracetmol" → "Paracetamol")
 * and normalized Levenshtein distance as a tie-breaker.
 *
 * Examples handled correctly:
 *   Paracetmol  → Paracetamol
 *   Amoxcillin  → Amoxicillin
 *   ibuprofen   → Ibuprofen (case-insensitive exact match, score 1.0)
 */
@Component
public class FuzzyMedicineMatchingService implements MedicineMatchingService {

    private static final Logger log = LoggerFactory.getLogger(FuzzyMedicineMatchingService.class);

    private final JaroWinklerSimilarity jaroWinklerSimilarity = new JaroWinklerSimilarity();
    private final LevenshteinDistance levenshteinDistance = LevenshteinDistance.getDefaultInstance();

    private final double similarityThreshold;
    private final int maxCandidatesPerMedicine;

    public FuzzyMedicineMatchingService(
            @Value("${medicine-matching.fuzzy-similarity-threshold:0.75}") double similarityThreshold,
            @Value("${medicine-matching.max-candidates-per-medicine:5}") int maxCandidatesPerMedicine) {
        this.similarityThreshold = similarityThreshold;
        this.maxCandidatesPerMedicine = maxCandidatesPerMedicine;
    }

    @Override
    public List<String> findClosestMatches(String extractedName, List<String> knownMedicineNames) {
        if (extractedName == null || extractedName.isBlank() || knownMedicineNames == null
                || knownMedicineNames.isEmpty()) {
            return List.of();
        }

        String normalizedExtracted = normalize(extractedName);

        List<ScoredMatch> scored = new ArrayList<>();
        for (String candidate : knownMedicineNames) {
            String normalizedCandidate = normalize(candidate);
            double score = computeSimilarity(normalizedExtracted, normalizedCandidate);
            if (score >= similarityThreshold) {
                scored.add(new ScoredMatch(candidate, score));
            }
        }

        scored.sort(Comparator.comparingDouble(ScoredMatch::score).reversed());

        List<String> results = scored.stream()
                .limit(maxCandidatesPerMedicine)
                .map(ScoredMatch::name)
                .toList();

        log.debug("FuzzyMatch: '{}' → {} matches (threshold={})",
                extractedName, results.size(), similarityThreshold);

        return results;
    }

    @Override
    public List<String> correctMedicineNames(List<ExtractedMedicineCandidate> candidates,
                                             List<String> knownMedicineNames) {
        List<String> corrected = new ArrayList<>();

        for (ExtractedMedicineCandidate candidate : candidates) {
            List<String> matches = findClosestMatches(candidate.getRawName(), knownMedicineNames);
            if (!matches.isEmpty()) {
                corrected.add(matches.get(0));
                log.debug("Corrected '{}' → '{}' (confidence={})",
                        candidate.getRawName(), matches.get(0), candidate.getConfidence());
            } else {
                // No good match found — keep the original normalized extraction
                corrected.add(candidate.getNormalizedName());
            }
        }

        return corrected;
    }

    private double computeSimilarity(String a, String b) {
        if (a.equals(b)) {
            return 1.0;
        }

        double jaroWinklerScore = jaroWinklerSimilarity.apply(a, b);

        int maxLen = Math.max(a.length(), b.length());
        double levenshteinScore = maxLen == 0
                ? 1.0
                : 1.0 - ((double) levenshteinDistance.apply(a, b) / maxLen);


        return (jaroWinklerScore * 0.6) + (levenshteinScore * 0.4);
    }

    private String normalize(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private record ScoredMatch(String name, double score) {}
}