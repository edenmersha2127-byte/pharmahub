package com.medicinelocator.auth.application.query.handler;

import com.medicinelocator.auth.application.dto.response.PagedResponse;
import com.medicinelocator.auth.application.dto.response.PharmacyAdminResponse;
import com.medicinelocator.auth.application.mapper.AdminMapper;
import com.medicinelocator.auth.application.query.GetPharmaciesQuery;
import com.medicinelocator.auth.application.service.PharmacyService;
import com.medicinelocator.auth.domain.model.Pharmacy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetPharmaciesHandler {

    private final PharmacyService pharmacyService;
    private final AdminMapper adminMapper;

    public GetPharmaciesHandler(PharmacyService pharmacyService, AdminMapper adminMapper) {
        this.pharmacyService = pharmacyService;
        this.adminMapper = adminMapper;
    }

    @Transactional(readOnly = true)
    public PagedResponse<PharmacyAdminResponse> handle(GetPharmaciesQuery query) {
        PageRequest pageable = PageRequest.of(query.getPage(), query.getSize());
        Page<Pharmacy> page = pharmacyService.findAll(query.getStatusFilter(), pageable);

        return new PagedResponse<>(
                page.getContent().stream().map(adminMapper::toPharmacyAdminResponse).toList(),
                query.getPage(),
                query.getSize(),
                page.getTotalElements()
        );
    }
}