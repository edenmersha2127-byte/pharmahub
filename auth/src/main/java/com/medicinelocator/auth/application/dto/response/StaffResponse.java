package com.medicinelocator.auth.application.dto.response;

import com.medicinelocator.auth.domain.enums.Permission;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class StaffResponse {

    private UUID id;
    private UUID pharmacyId;
    private String email;
    private String fullName;
    private String jobTitle;
    private Set<Permission> permissions;
    private boolean active;
    private LocalDateTime createdAt;

    public StaffResponse() {
    }

    public StaffResponse(UUID id, UUID pharmacyId, String email, String fullName,
                         String jobTitle, Set<Permission> permissions, boolean active,
                         LocalDateTime createdAt) {
        this.id = id;
        this.pharmacyId = pharmacyId;
        this.email = email;
        this.fullName = fullName;
        this.jobTitle = jobTitle;
        this.permissions = permissions;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public Set<Permission> getPermissions() { return permissions; }
    public void setPermissions(Set<Permission> permissions) { this.permissions = permissions; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
