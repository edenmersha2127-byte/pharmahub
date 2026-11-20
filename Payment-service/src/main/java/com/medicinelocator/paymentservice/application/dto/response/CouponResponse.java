package com.medicinelocator.paymentservice.application.dto.response;

import com.medicinelocator.paymentservice.domain.enums.CouponDiscountType;
import com.medicinelocator.paymentservice.domain.model.Coupon;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CouponResponse(
        UUID id,
        String code,
        CouponDiscountType discountType,
        BigDecimal discountValue,
        Integer maxRedemptions,
        int timesRedeemed,
        LocalDateTime expiresAt,
        boolean active) {

    public static CouponResponse from(Coupon c) {
        return new CouponResponse(
                c.getId(), c.getCode(), c.getDiscountType(), c.getDiscountValue(),
                c.getMaxRedemptions(), c.getTimesRedeemed(), c.getExpiresAt(), c.isActive());
    }
}
