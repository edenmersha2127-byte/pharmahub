package com.medicinelocator.auth.application.mapper;

import com.medicinelocator.auth.application.dto.response.StaffResponse;
import com.medicinelocator.auth.domain.model.Staff;
import org.springframework.stereotype.Component;

@Component
public class StaffMapper {

    public StaffResponse toResponse(Staff staff) {
        return new StaffResponse(
                staff.getId(),
                staff.getPharmacyId(),
                staff.getEmail(),
                staff.getFullName(),
                staff.getJobTitle(),
                staff.getPermissions(),
                staff.isActive(),
                staff.getCreatedAt()
        );
    }
}
