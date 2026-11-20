package com.medicinelocator.paymentservice.application.dto.response;

import com.medicinelocator.paymentservice.domain.model.Subscription;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Compact access answer for service-to-service checks.
 */
public record SubscriptionStatusResponse(
        UUID pharmacyId,
        String status,
        LocalDateTime subscribedUntil,
        boolean active) {

    public static SubscriptionStatusResponse of(UUID pharmacyId, Subscription current) {
        if (current == null) {
            return new SubscriptionStatusResponse(pharmacyId, "NONE", null, false);
        }
        return new SubscriptionStatusResponse(
                pharmacyId, current.getStatus().name(), current.getEndDate(), current.grantsAccess());
    }
}
