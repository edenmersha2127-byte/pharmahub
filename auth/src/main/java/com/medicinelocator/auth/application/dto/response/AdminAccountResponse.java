package com.medicinelocator.auth.application.dto.response;

import com.medicinelocator.auth.domain.model.Admin;

import java.util.UUID;

/**
 * Read-only view of an admin/sub-admin account.
 */
public class AdminAccountResponse {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;

    public AdminAccountResponse() {
    }

    public AdminAccountResponse(UUID id, String email, String firstName,
                                String lastName, String role) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public static AdminAccountResponse from(Admin admin) {
        return new AdminAccountResponse(
                admin.getId(),
                admin.getEmail(),
                admin.getFirstName(),
                admin.getLastName(),
                admin.getRole().name());
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
