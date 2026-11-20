package com.medicinelocator.auth.application.command.handler;

import com.medicinelocator.auth.application.command.ChangePasswordCommand;
import com.medicinelocator.auth.application.service.CustomerService;
import com.medicinelocator.auth.application.service.PasswordHasher;
import com.medicinelocator.auth.domain.enums.Role;
import com.medicinelocator.auth.domain.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ChangePasswordHandler {

    private static final Logger log = LoggerFactory.getLogger(ChangePasswordHandler.class);

    private final CustomerService customerService;
    private final PasswordHasher passwordHasher;

    public ChangePasswordHandler(CustomerService customerService, PasswordHasher passwordHasher) {
        this.customerService = customerService;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public void handle(ChangePasswordCommand command) {
        if (command.getRole() != Role.CUSTOMER) {
            throw new IllegalArgumentException("Password change is only supported for customer accounts");
        }

        Customer customer = customerService.findById(command.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        if (!passwordHasher.matches(command.getCurrentPassword(), customer.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (passwordHasher.matches(command.getNewPassword(), customer.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from the current password");
        }

        customer.setPasswordHash(passwordHasher.hash(command.getNewPassword()));
        customerService.update(customer);

        log.info("Password changed for customer id={}", customer.getId());
    }
}
