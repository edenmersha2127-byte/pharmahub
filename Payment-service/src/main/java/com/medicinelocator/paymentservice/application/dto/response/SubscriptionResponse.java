package com.medicinelocator.paymentservice.application.dto.response;

import com.medicinelocator.paymentservice.domain.enums.BillingCycle;
import com.medicinelocator.paymentservice.domain.enums.SubscriptionStatus;
import com.medicinelocator.paymentservice.domain.model.Subscription;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        UUID pharmacyId,
        BillingCycle plan,
        SubscriptionStatus status,
        BigDecimal amount,
        LocalDateTime startDate,
        LocalDateTime endDate,
        boolean active) {

    public static SubscriptionResponse from(Subscription s) {
        return new SubscriptionResponse(
                s.getId(), s.getPharmacyId(), s.getPlan(), s.getStatus(), s.getAmount(),
                s.getStartDate(), s.getEndDate(), s.grantsAccess());
    }
}
