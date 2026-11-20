package com.medicinelocator.salesservice.infrastructure.security;

import com.medicinelocator.salesservice.domain.exception.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Central authorization checks for pharmacy-scoped operations. A PHARMACY owner
 * implicitly holds every permission; PHARMACY_STAFF must carry the specific
 * capability box granted by the owner.
 */
@Component
public class SecurityUtils {

    public void requirePharmacyAccess(CurrentUser currentUser, UUID targetPharmacyId,
                                      String requiredPermission) {
        boolean isPharmacyRole = currentUser.isPharmacy() || currentUser.isPharmacyStaff();
        if (!isPharmacyRole) {
            throw new AccessDeniedException(
                    "Access denied: only PHARMACY or PHARMACY_STAFF accounts can access sales operations");
        }

        UUID effectivePharmacyId = currentUser.getEffectivePharmacyId();
        if (effectivePharmacyId == null || !effectivePharmacyId.equals(targetPharmacyId)) {
            throw new AccessDeniedException(
                    "Access denied: you can only act on your own pharmacy. Attempted pharmacyId: "
                            + targetPharmacyId);
        }

        if (currentUser.isPharmacy()) {
            return; // owner has all permissions
        }

        if (requiredPermission != null && !currentUser.hasPermission(requiredPermission)) {
            throw new AccessDeniedException(
                    "Access denied: missing required permission '" + requiredPermission + "'");
        }
    }

    /**
     * Resolve the caller's effective pharmacy id (owner or staff), enforcing that
     * they are a pharmacy-side account and hold {@code requiredPermission}.
     */
    public UUID requirePharmacyContext(CurrentUser currentUser, String requiredPermission) {
        UUID pharmacyId = currentUser.getEffectivePharmacyId();
        if (pharmacyId == null) {
            throw new AccessDeniedException(
                    "Access denied: only PHARMACY or PHARMACY_STAFF accounts can access sales operations");
        }
        requirePharmacyAccess(currentUser, pharmacyId, requiredPermission);
        return pharmacyId;
    }
}
