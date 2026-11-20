package com.medicinelocator.paymentservice.application.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Port to the Auth Service for (a) looking up pharmacy contact details needed
 * for a Chapa checkout, and (b) pushing the pharmacy's subscription state so
 * auth can gate login/search (mirrors the suspension mechanism).
 */
public interface PharmacyAccessService {

    Optional<PharmacyInfo> getPharmacy(UUID pharmacyId);

    /**
     * Tell auth the pharmacy's current subscription state. Best-effort: logs on
     * failure rather than failing the payment that already succeeded.
     *
     * @param status         TRIAL | ACTIVE | EXPIRED
     * @param subscribedUntil access deadline (null when none)
     */
    void pushSubscriptionState(UUID pharmacyId, String status, LocalDateTime subscribedUntil);

    record PharmacyInfo(UUID pharmacyId, String name, String email) {
    }
}
