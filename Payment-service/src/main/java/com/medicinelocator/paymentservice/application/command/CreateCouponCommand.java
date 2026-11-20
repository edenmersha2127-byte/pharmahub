package com.medicinelocator.paymentservice.application.command;

import com.medicinelocator.paymentservice.domain.enums.CouponDiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreateCouponCommand {

    private final String code;
    private final CouponDiscountType discountType;
    private final BigDecimal discountValue;
    private final Integer maxRedemptions;
    private final LocalDateTime expiresAt;

    public CreateCouponCommand(String code, CouponDiscountType discountType, BigDecimal discountValue,
                               Integer maxRedemptions, LocalDateTime expiresAt) {
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.maxRedemptions = maxRedemptions;
        this.expiresAt = expiresAt;
    }

    public String getCode() { return code; }
    public CouponDiscountType getDiscountType() { return discountType; }
    public BigDecimal getDiscountValue() { return discountValue; }
    public Integer getMaxRedemptions() { return maxRedemptions; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}
