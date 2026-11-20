package com.medicinelocator.paymentservice.application.dto.response;

import com.medicinelocator.paymentservice.domain.enums.BillingCycle;

import java.math.BigDecimal;

/**
 * A subscription plan the frontend can display (authoritative prices from config).
 */
public record PlanResponse(
        BillingCycle plan,
        BigDecimal price,
        int durationDays,
        String currency) {
}
