package com.medicinelocator.prescription.infrastructure.security;

import java.util.UUID;

public class CurrentUser {

    private final UUID userId;
    private final String role;
    private final String email;
    private final UUID pharmacyId; // nullable; carried in X-Pharmacy-Id for PHARMACY_STAFF

    public CurrentUser(UUID userId, String role, String email, UUID pharmacyId) {
        this.userId = userId;
        this.role = role;
        this.email = email;
        this.pharmacyId = pharmacyId;
    }

    public boolean isAdmin()         { return RoleConstants.ROLE_ADMIN.equals(role); }
    public boolean isPharmacy()      { return RoleConstants.ROLE_PHARMACY.equals(role); }
    public boolean isPharmacyStaff() { return RoleConstants.ROLE_PHARMACY_STAFF.equals(role); }
    public boolean isCustomer()      { return RoleConstants.ROLE_CUSTOMER.equals(role); }

    /**
     * The pharmacy this user acts on behalf of: for a PHARMACY owner that is
     * their own userId; for PHARMACY_STAFF it is the X-Pharmacy-Id they carry.
     * Null for non-pharmacy accounts.
     */
    public UUID getEffectivePharmacyId() {
        if (isPharmacy()) {
            return userId;
        }
        if (isPharmacyStaff()) {
            return pharmacyId;
        }
        return null;
    }

    public UUID getUserId() { return userId; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public UUID getPharmacyId() { return pharmacyId; }
}
