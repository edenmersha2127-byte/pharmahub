package com.medicinelocator.auth.application.dto.response;

import java.util.UUID;

public class UserProfileResponse {

    private UUID id;
    private String email;
    private String role;
    private String displayName;
    private boolean emailVerified;
    private String firstName;
    private String lastName;
    private String phoneNumber;

    public UserProfileResponse(UUID id, String email, String role, String displayName,
                               boolean emailVerified, String firstName, String lastName,
                               String phoneNumber) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.displayName = displayName;
        this.emailVerified = emailVerified;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getDisplayName() { return displayName; }
    public boolean isEmailVerified() { return emailVerified; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhoneNumber() { return phoneNumber; }
}
