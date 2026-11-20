package com.medicinelocator.salesservice.infrastructure.security;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class CurrentUser {

    private final UUID userId;
    private final String role;
    private final String email;
    private final UUID pharmacyId;
    private final Set<String> permissions;

    public CurrentUser(UUID userId, String role, String email,
                       UUID pharmacyId, Set<String> permissions) {
        this.userId = userId;
        this.role = role;
        this.email = email;
        this.pharmacyId = pharmacyId;
        this.permissions = permissions != null ? permissions : Collections.emptySet();
    }

    public boolean isAdmin() {
        return RoleConstants.ROLE_ADMIN.equals(role);
    }

    public boolean isPharmacy() {
        return RoleConstants.ROLE_PHARMACY.equals(role);
    }

    public boolean isPharmacyStaff() {
        return RoleConstants.ROLE_PHARMACY_STAFF.equals(role);
    }

    /**
     * The pharmacy this user acts on behalf of. For a PHARMACY owner this equals
     * their own userId (also carried in X-Pharmacy-Id); for PHARMACY_STAFF it is
     * the employer's id. Null for CUSTOMER / ADMIN.
     */
    public UUID getEffectivePharmacyId() {
        if (pharmacyId != null) {
            return pharmacyId;
        }
        return isPharmacy() ? userId : null;
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public UUID getUserId() { return userId; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public UUID getPharmacyId() { return pharmacyId; }
    public Set<String> getPermissions() { return permissions; }
}
