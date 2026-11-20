package com.medicinelocator.paymentservice.application.dto.response;

import java.math.BigDecimal;

/**
 * Preview of what a coupon does to a plan's price, shown before checkout.
 * When {@code valid} is false, {@code message} explains why and the amounts
 * fall back to the undiscounted price.
 */
public record CouponPreviewResponse(
        boolean valid,
        String message,
        BigDecimal originalAmount,
        BigDecimal discountedAmount) {
}
