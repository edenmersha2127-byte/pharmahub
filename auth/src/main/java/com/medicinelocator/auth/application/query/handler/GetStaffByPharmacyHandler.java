package com.medicinelocator.auth.application.query.handler;

import com.medicinelocator.auth.application.dto.response.StaffResponse;
import com.medicinelocator.auth.application.mapper.StaffMapper;
import com.medicinelocator.auth.application.query.GetStaffByPharmacyQuery;
import com.medicinelocator.auth.application.service.StaffService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetStaffByPharmacyHandler {

    private final StaffService staffService;
    private final StaffMapper staffMapper;

    public GetStaffByPharmacyHandler(StaffService staffService, StaffMapper staffMapper) {
        this.staffService = staffService;
        this.staffMapper = staffMapper;
    }

    public List<StaffResponse> handle(GetStaffByPharmacyQuery query) {
        return staffService.findByPharmacyId(query.getPharmacyId())
                .stream()
                .map(staffMapper::toResponse)
                .toList();
    }
}
