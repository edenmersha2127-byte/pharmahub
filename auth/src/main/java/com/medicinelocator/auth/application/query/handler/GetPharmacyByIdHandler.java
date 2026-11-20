package com.medicinelocator.auth.application.query.handler;

import com.medicinelocator.auth.application.dto.response.PharmacyAdminResponse;
import com.medicinelocator.auth.application.mapper.AdminMapper;
import com.medicinelocator.auth.application.query.GetPharmacyByIdQuery;
import com.medicinelocator.auth.application.service.PharmacyService;
import com.medicinelocator.auth.domain.exception.PharmacyNotFoundException;
import com.medicinelocator.auth.domain.model.Pharmacy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetPharmacyByIdHandler {

    private final PharmacyService pharmacyService;
    private final AdminMapper adminMapper;

    public GetPharmacyByIdHandler(PharmacyService pharmacyService, AdminMapper adminMapper) {
        this.pharmacyService = pharmacyService;
        this.adminMapper = adminMapper;
    }

    @Transactional(readOnly = true)
    public PharmacyAdminResponse handle(GetPharmacyByIdQuery query) {
        Pharmacy pharmacy = pharmacyService.findById(query.getPharmacyId())
                .orElseThrow(() -> new PharmacyNotFoundException(query.getPharmacyId()));

        return adminMapper.toPharmacyAdminResponse(pharmacy);
    }
}