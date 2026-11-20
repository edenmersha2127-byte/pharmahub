package com.medicinelocator.auth.application.query.handler;

import com.medicinelocator.auth.application.dto.response.PharmacySummaryResponse;
import com.medicinelocator.auth.application.query.GetAllPharmaciesQuery;
import com.medicinelocator.auth.application.service.PharmacyService;
import com.medicinelocator.auth.domain.model.Pharmacy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class GetAllPharmaciesHandler {

    private final PharmacyService pharmacyService;

    public GetAllPharmaciesHandler(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    @Transactional(readOnly = true)
    public List<PharmacySummaryResponse> handle(GetAllPharmaciesQuery query) {
        return pharmacyService.findAllUnpaged().stream()
                .map(this::toSummary)
                .toList();
    }

    private PharmacySummaryResponse toSummary(Pharmacy pharmacy) {
        return new PharmacySummaryResponse(
                pharmacy.getId(),
                pharmacy.getPharmacyName(),
                pharmacy.getEmail(),
                pharmacy.getPharmacyStatus() != null
                        ? pharmacy.getPharmacyStatus().name()
                        : null
        );
    }
}
