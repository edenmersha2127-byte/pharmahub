package com.medicinelocator.auth.domain.model;

import com.medicinelocator.auth.domain.enums.Permission;
import com.medicinelocator.auth.domain.enums.Role;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * A pharmacy employee account. Belongs to exactly ONE pharmacy and carries a
 * subset of {@link Permission}s chosen by that pharmacy's owner. Staff log in
 * with their own e-mail/password and act on behalf of their pharmacy; their
 * effective pharmacyId + permissions are embedded in the JWT so downstream
 * services can authorise each action without calling back to auth.
 */
public class Staff {

    private UUID id;
    private UUID pharmacyId;
    private String email;
    private String passwordHash;
    private String fullName;
    private String jobTitle;              // free-text label the owner picks (e.g. "Cashier")
    private Set<Permission> permissions;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Staff() {
        this.permissions = EnumSet.noneOf(Permission.class);
    }

    public Staff(UUID id, UUID pharmacyId, String email, String passwordHash,
                 String fullName, String jobTitle, Set<Permission> permissions,
                 boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.pharmacyId = pharmacyId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.jobTitle = jobTitle;
        this.permissions = permissions != null ? EnumSet.copyOf(permissions) : EnumSet.noneOf(Permission.class);
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Role getRole() {
        return Role.PHARMACY_STAFF;
    }

    /** Replace the whole permission set (owner re-ticks the boxes). */
    public void updatePermissions(Set<Permission> newPermissions) {
        this.permissions = newPermissions != null
                ? EnumSet.copyOf(newPermissions)
                : EnumSet.noneOf(Permission.class);
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProfile(String fullName, String jobTitle) {
        this.fullName = fullName;
        this.jobTitle = jobTitle;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasPermission(Permission permission) {
        return permissions != null && permissions.contains(permission);
    }

    public boolean belongsTo(UUID pharmacyId) {
        return this.pharmacyId != null && this.pharmacyId.equals(pharmacyId);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public Set<Permission> getPermissions() { return permissions; }
    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions != null
                ? EnumSet.copyOf(permissions)
                : EnumSet.noneOf(Permission.class);
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
