package com.medicinelocator.prescription.infrastructure.security;

import com.medicinelocator.prescription.domain.exception.InvalidPrescriptionException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityUtils {

    public void requireCustomerOrAdmin(CurrentUser currentUser) {
        if (!currentUser.isCustomer() && !currentUser.isAdmin()) {
            throw new InvalidPrescriptionException(
                    "Access denied: only CUSTOMER or ADMIN can upload prescriptions");
        }
    }

    public void requireCustomerOwnershipOrAdmin(CurrentUser currentUser, UUID ownerId) {
        if (currentUser.isAdmin()) {
            return;
        }
        if (!currentUser.getUserId().equals(ownerId)) {
            throw new InvalidPrescriptionException(
                    "Access denied: you can only access your own prescriptions");
        }
    }

    /**
     * Ensures the caller is a pharmacy-side account and returns the pharmacy it
     * acts on behalf of. Used by the pharmacy dispense-scan flow.
     */
    public UUID requirePharmacyContext(CurrentUser currentUser) {
        if (!currentUser.isPharmacy() && !currentUser.isPharmacyStaff()) {
            throw new InvalidPrescriptionException(
                    "Access denied: only PHARMACY or PHARMACY_STAFF accounts can scan prescriptions for dispensing");
        }
        UUID pharmacyId = currentUser.getEffectivePharmacyId();
        if (pharmacyId == null) {
            throw new InvalidPrescriptionException(
                    "Access denied: could not resolve the pharmacy context for this account");
        }
        return pharmacyId;
    }
}