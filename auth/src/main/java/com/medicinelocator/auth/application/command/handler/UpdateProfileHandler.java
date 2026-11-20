package com.medicinelocator.auth.application.command.handler;

import com.medicinelocator.auth.application.command.UpdateProfileCommand;
import com.medicinelocator.auth.application.dto.response.UserProfileResponse;
import com.medicinelocator.auth.application.service.CustomerService;
import com.medicinelocator.auth.domain.enums.Role;
import com.medicinelocator.auth.domain.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateProfileHandler {

    private static final Logger log = LoggerFactory.getLogger(UpdateProfileHandler.class);

    private final CustomerService customerService;

    public UpdateProfileHandler(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Transactional
    public UserProfileResponse handle(UpdateProfileCommand command) {
        if (command.getRole() != Role.CUSTOMER) {
            throw new IllegalArgumentException("Profile update is only supported for customer accounts");
        }

        Customer customer = customerService.findById(command.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        customer.setFirstName(command.getFirstName());
        customer.setLastName(command.getLastName());
        customer.setPhoneNumber(command.getPhoneNumber());

        Customer updated = customerService.update(customer);

        log.info("Profile updated for customer id={}", updated.getId());

        return new UserProfileResponse(
                updated.getId(),
                updated.getEmail(),
                Role.CUSTOMER.name(),
                updated.getFirstName() + " " + updated.getLastName(),
                updated.isEmailVerified(),
                updated.getFirstName(),
                updated.getLastName(),
                updated.getPhoneNumber()
        );
    }
}
