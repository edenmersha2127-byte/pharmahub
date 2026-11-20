package com.medicinelocator.auth.application.query.handler;

import com.medicinelocator.auth.application.dto.response.PharmacyProfileResponse;
import com.medicinelocator.auth.application.query.GetPharmacyProfileQuery;
import com.medicinelocator.auth.application.service.PharmacyService;
import com.medicinelocator.auth.domain.model.Pharmacy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetPharmacyProfileHandler {

    private final PharmacyService pharmacyService;

    public GetPharmacyProfileHandler(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    @Transactional(readOnly = true)
    public PharmacyProfileResponse handle(GetPharmacyProfileQuery query) {
        Pharmacy pharmacy = pharmacyService.findById(query.getPharmacyId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pharmacy not found with id: " + query.getPharmacyId()));

        return new PharmacyProfileResponse(
                pharmacy.getId(),
                pharmacy.getPharmacyName(),
                pharmacy.getEmail(),
                pharmacy.getAddress(),
                pharmacy.getCity(),
                pharmacy.getLatitude(),
                pharmacy.getLongitude(),
                pharmacy.getLicenseNumber(),
                pharmacy.getPhoneNumber(),
                pharmacy.isOpen()
        );
    }
}