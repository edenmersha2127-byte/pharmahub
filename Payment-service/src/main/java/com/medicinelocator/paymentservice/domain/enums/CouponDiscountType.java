package com.medicinelocator.paymentservice.domain.enums;

/**
 * How a coupon reduces the price.
 * PERCENTAGE -> value is a percent off (e.g. 20 = 20% off)
 * FIXED      -> value is a flat amount off in ETB (e.g. 300 = 300 birr off)
 */
public enum CouponDiscountType {
    PERCENTAGE,
    FIXED
}
