package com.medicinelocator.auth.application.command;

import com.medicinelocator.auth.domain.enums.Role;

import java.util.UUID;

public class UpdateProfileCommand {

    private final UUID userId;
    private final Role role;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;

    public UpdateProfileCommand(UUID userId, Role role, String firstName,
                                String lastName, String phoneNumber) {
        this.userId = userId;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }

    public UUID getUserId() { return userId; }
    public Role getRole() { return role; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhoneNumber() { return phoneNumber; }
}
