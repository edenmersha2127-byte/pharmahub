package com.medicinelocator.auth.application.command.handler;

import com.medicinelocator.auth.application.command.CreateReportAdminCommand;
import com.medicinelocator.auth.application.service.AdminService;
import com.medicinelocator.auth.application.service.PasswordHasher;
import com.medicinelocator.auth.domain.enums.Role;
import com.medicinelocator.auth.domain.model.Admin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a REPORT_ADMIN sub-admin account. Invoked by the super admin
 * (ROLE_ADMIN) only. The new account can manage reports/moderation but nothing
 * else.
 */
@Component
public class CreateReportAdminHandler {

    private static final Logger log = LoggerFactory.getLogger(CreateReportAdminHandler.class);

    private final AdminService adminService;
    private final PasswordHasher passwordHasher;

    public CreateReportAdminHandler(AdminService adminService, PasswordHasher passwordHasher) {
        this.adminService = adminService;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public Admin handle(CreateReportAdminCommand command) {
        if (adminService.existsByEmail(command.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + command.getEmail());
        }

        Admin admin = Admin.create(
                command.getEmail(),
                passwordHasher.hash(command.getPassword()),
                command.getFirstName(),
                command.getLastName(),
                Role.REPORT_ADMIN);

        Admin saved = adminService.save(admin);
        log.info("Report admin created: id={} email={}", saved.getId(), saved.getEmail());
        return saved;
    }
}
