package com.medicinelocator.auth.application.command;

import com.medicinelocator.auth.domain.enums.Permission;

import java.util.Set;
import java.util.UUID;

public class UpdateStaffPermissionsCommand {

    private final UUID pharmacyId;
    private final UUID staffId;
    private final String fullName;
    private final String jobTitle;
    private final Set<Permission> permissions;

    public UpdateStaffPermissionsCommand(UUID pharmacyId, UUID staffId, String fullName,
                                         String jobTitle, Set<Permission> permissions) {
        this.pharmacyId = pharmacyId;
        this.staffId = staffId;
        this.fullName = fullName;
        this.jobTitle = jobTitle;
        this.permissions = permissions;
    }

    public UUID getPharmacyId() { return pharmacyId; }
    public UUID getStaffId() { return staffId; }
    public String getFullName() { return fullName; }
    public String getJobTitle() { return jobTitle; }
    public Set<Permission> getPermissions() { return permissions; }
}
