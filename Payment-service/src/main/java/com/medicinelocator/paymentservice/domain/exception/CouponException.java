package com.medicinelocator.paymentservice.domain.exception;

/**
 * Raised when a coupon code is invalid, inactive, expired, or fully redeemed.
 */
public class CouponException extends RuntimeException {

    public CouponException(String message) {
        super(message);
    }
}
