package com.medicinelocator.prescription.application.service;

import com.medicinelocator.prescription.domain.model.ExtractedMedicineCandidate;

import java.util.List;

public interface MedicineMatchingService {


    List<String> findClosestMatches(String extractedName, List<String> knownMedicineNames);


    List<String> correctMedicineNames(List<ExtractedMedicineCandidate> candidates,
                                      List<String> knownMedicineNames);
}