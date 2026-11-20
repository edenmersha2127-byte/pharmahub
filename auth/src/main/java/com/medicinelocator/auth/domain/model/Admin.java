package com.medicinelocator.auth.domain.model;

import com.medicinelocator.auth.domain.enums.AccountStatus;
import com.medicinelocator.auth.domain.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public class Admin {

    private UUID id;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private Role role = Role.ADMIN;
    private AccountStatus accountStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Admin() {
    }

    public Admin(UUID id, String email, String passwordHash, String firstName,
                 String lastName, AccountStatus accountStatus,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Factory for creating a new admin/sub-admin with an explicit role. */
    public static Admin create(String email, String passwordHash, String firstName,
                               String lastName, Role role) {
        Admin admin = new Admin();
        admin.email = email;
        admin.passwordHash = passwordHash;
        admin.firstName = firstName;
        admin.lastName = lastName;
        admin.role = role;
        admin.accountStatus = AccountStatus.ACTIVE;
        return admin;
    }

    /** The admin's role. Defaults to super ADMIN for legacy rows with no role set. */
    public Role getRole() {
        return role != null ? role : Role.ADMIN;
    }

    public void setRole(Role role) { this.role = role; }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}