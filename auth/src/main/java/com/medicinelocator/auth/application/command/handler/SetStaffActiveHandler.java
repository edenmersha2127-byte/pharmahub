package com.medicinelocator.auth.application.command.handler;

import com.medicinelocator.auth.application.command.SetStaffActiveCommand;
import com.medicinelocator.auth.application.service.StaffService;
import com.medicinelocator.auth.domain.exception.StaffNotFoundException;
import com.medicinelocator.auth.domain.model.Staff;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SetStaffActiveHandler {

    private final StaffService staffService;

    public SetStaffActiveHandler(StaffService staffService) {
        this.staffService = staffService;
    }

    @Transactional
    public Staff handle(SetStaffActiveCommand command) {
        Staff staff = staffService.findById(command.getStaffId())
                .orElseThrow(() -> new StaffNotFoundException("Staff not found: " + command.getStaffId()));

        if (!staff.belongsTo(command.getPharmacyId())) {
            throw new StaffNotFoundException("Staff not found in your pharmacy: " + command.getStaffId());
        }

        if (command.isActive()) {
            staff.activate();
        } else {
            staff.deactivate();
        }
        return staffService.update(staff);
    }
}
