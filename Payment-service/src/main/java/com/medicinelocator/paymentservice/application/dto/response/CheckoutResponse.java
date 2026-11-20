package com.medicinelocator.paymentservice.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Returned when a pharmacy starts a subscription. If checkoutUrl is present the
 * pharmacy must be redirected there to pay; if null (e.g. a 100%-off coupon),
 * the subscription is already ACTIVE.
 */
public record CheckoutResponse(
        UUID subscriptionId,
        String txRef,
        BigDecimal amount,
        String checkoutUrl,
        String status) {
}
