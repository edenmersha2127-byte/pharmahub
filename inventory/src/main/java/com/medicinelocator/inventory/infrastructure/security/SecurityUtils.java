package com.medicinelocator.inventory.infrastructure.security;

import com.medicinelocator.inventory.domain.exception.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityUtils {

    /**
     * Enforces that the caller may act on the given pharmacy's inventory.
     *
     * <ul>
     *   <li>Role must be PHARMACY or PHARMACY_STAFF, else access is denied.</li>
     *   <li>The caller's effective pharmacyId must equal {@code targetPharmacyId}.</li>
     *   <li>A PHARMACY owner implicitly has ALL permissions.</li>
     *   <li>A PHARMACY_STAFF must carry {@code requiredPermission} in its granted set
     *       (a {@code null} permission means no specific permission is required).</li>
     * </ul>
     */
    public void requirePharmacyAccess(CurrentUser currentUser, UUID targetPharmacyId,
                                      String requiredPermission) {
        boolean isPharmacyRole = currentUser.isPharmacy() || currentUser.isPharmacyStaff();
        if (!isPharmacyRole) {
            throw new AccessDeniedException(
                    "Access denied: only PHARMACY or PHARMACY_STAFF accounts can access pharmacy inventory");
        }

        UUID effectivePharmacyId = currentUser.getEffectivePharmacyId();
        if (effectivePharmacyId == null || !effectivePharmacyId.equals(targetPharmacyId)) {
            throw new AccessDeniedException(
                    "Access denied: you can only access your own pharmacy's inventory. "
                            + "Attempted pharmacyId: " + targetPharmacyId);
        }

        if (currentUser.isPharmacy()) {
            return; // owner has all permissions
        }

        // PHARMACY_STAFF
        if (requiredPermission != null && !currentUser.hasPermission(requiredPermission)) {
            throw new AccessDeniedException(
                    "Access denied: missing required permission '" + requiredPermission + "'");
        }
    }

    /**
     * Enforces that the caller owns (or is staff of) the given pharmacy, with no
     * specific permission requirement. Retained for backward compatibility.
     */
    public void requirePharmacyOwnership(CurrentUser currentUser, UUID pharmacyId) {
        requirePharmacyAccess(currentUser, pharmacyId, null);
    }

    /**
     * Allows any authenticated user (CUSTOMER, PHARMACY, PHARMACY_STAFF, ADMIN).
     * Called on read-only / search endpoints.
     */
    public void requireAuthenticated(CurrentUser currentUser) {
        if (currentUser.getUserId() == null) {
            throw new AccessDeniedException("Access denied: authentication required");
        }
        String role = currentUser.getRole();
        if (!RoleConstants.ROLE_CUSTOMER.equals(role)
                && !RoleConstants.ROLE_PHARMACY.equals(role)
                && !RoleConstants.ROLE_PHARMACY_STAFF.equals(role)
                && !RoleConstants.ROLE_ADMIN.equals(role)) {
            throw new AccessDeniedException("Access denied: unknown role '" + role + "'");
        }
    }
}
