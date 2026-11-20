package com.medicinelocator.auth.application.dto.request;

import com.medicinelocator.auth.domain.enums.Permission;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public class UpdateStaffPermissionsRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String jobTitle;

    /** Replaces the staff member's entire permission set. */
    private Set<Permission> permissions;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public Set<Permission> getPermissions() { return permissions; }
    public void setPermissions(Set<Permission> permissions) { this.permissions = permissions; }
}
