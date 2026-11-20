package com.medicinelocator.auth.application.command.handler;

import com.medicinelocator.auth.application.command.CreateStaffCommand;
import com.medicinelocator.auth.application.service.AdminService;
import com.medicinelocator.auth.application.service.CustomerService;
import com.medicinelocator.auth.application.service.PasswordHasher;
import com.medicinelocator.auth.application.service.PharmacyService;
import com.medicinelocator.auth.application.service.StaffService;
import com.medicinelocator.auth.domain.model.Pharmacy;
import com.medicinelocator.auth.domain.model.Staff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class CreateStaffHandler {

    private static final Logger log = LoggerFactory.getLogger(CreateStaffHandler.class);

    private final StaffService staffService;
    private final PharmacyService pharmacyService;
    private final CustomerService customerService;
    private final AdminService adminService;
    private final PasswordHasher passwordHasher;

    public CreateStaffHandler(StaffService staffService,
                              PharmacyService pharmacyService,
                              CustomerService customerService,
                              AdminService adminService,
                              PasswordHasher passwordHasher) {
        this.staffService = staffService;
        this.pharmacyService = pharmacyService;
        this.customerService = customerService;
        this.adminService = adminService;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public Staff handle(CreateStaffCommand command) {
        // The owning pharmacy must exist and be approved before it can hire staff.
        Pharmacy pharmacy = pharmacyService.findById(command.getPharmacyId())
                .orElseThrow(() -> new IllegalArgumentException("Pharmacy not found: " + command.getPharmacyId()));
        if (!pharmacy.isApproved()) {
            throw new IllegalArgumentException("Pharmacy is not approved and cannot create staff yet.");
        }

        // E-mail must be globally unique — login resolves an account by e-mail
        // across customers / pharmacies / admins / staff.
        String email = command.getEmail();
        if (staffService.existsByEmail(email)
                || pharmacyService.existsByEmail(email)
                || customerService.existsByEmail(email)
                || adminService.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }

        Staff staff = new Staff(
                null,
                command.getPharmacyId(),
                email,
                passwordHasher.hash(command.getPassword()),
                command.getFullName(),
                command.getJobTitle(),
                command.getPermissions(),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Staff saved = staffService.save(staff);
        log.info("Staff created: email={} pharmacyId={}", email, command.getPharmacyId());
        return saved;
    }
}
