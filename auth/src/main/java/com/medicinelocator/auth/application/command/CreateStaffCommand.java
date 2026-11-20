package com.medicinelocator.auth.application.command;

import com.medicinelocator.auth.domain.enums.Permission;

import java.util.Set;
import java.util.UUID;

public class CreateStaffCommand {

    private final UUID pharmacyId;
    private final String email;
    private final String password;
    private final String fullName;
    private final String jobTitle;
    private final Set<Permission> permissions;

    public CreateStaffCommand(UUID pharmacyId, String email, String password,
                              String fullName, String jobTitle, Set<Permission> permissions) {
        this.pharmacyId = pharmacyId;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.jobTitle = jobTitle;
        this.permissions = permissions;
    }

    public UUID getPharmacyId() { return pharmacyId; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getJobTitle() { return jobTitle; }
    public Set<Permission> getPermissions() { return permissions; }
}
