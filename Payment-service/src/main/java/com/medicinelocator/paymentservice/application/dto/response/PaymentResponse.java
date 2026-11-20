package com.medicinelocator.paymentservice.application.dto.response;

import com.medicinelocator.paymentservice.domain.enums.PaymentStatus;
import com.medicinelocator.paymentservice.domain.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID subscriptionId,
        UUID pharmacyId,
        String txRef,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String couponCode,
        LocalDateTime createdAt) {

    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(
                p.getId(), p.getSubscriptionId(), p.getPharmacyId(), p.getTxRef(), p.getAmount(),
                p.getCurrency(), p.getStatus(), p.getCouponCode(), p.getCreatedAt());
    }
}
