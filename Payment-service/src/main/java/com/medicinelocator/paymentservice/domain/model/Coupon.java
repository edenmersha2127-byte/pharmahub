package com.medicinelocator.paymentservice.domain.model;

import com.medicinelocator.paymentservice.domain.enums.CouponDiscountType;
import com.medicinelocator.paymentservice.domain.exception.CouponException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A discount code the super admin creates and shares with pharmacies.
 * Pure domain object with the discount + validity rules.
 */
public class Coupon {

    private UUID id;
    private String code;
    private CouponDiscountType discountType;
    private BigDecimal discountValue;     // percent (0-100) or fixed ETB
    private Integer maxRedemptions;       // null = unlimited
    private int timesRedeemed;
    private LocalDateTime expiresAt;      // null = never expires
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Coupon() {
    }

    public Coupon(UUID id, String code, CouponDiscountType discountType, BigDecimal discountValue,
                  Integer maxRedemptions, int timesRedeemed, LocalDateTime expiresAt,
                  boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.maxRedemptions = maxRedemptions;
        this.timesRedeemed = timesRedeemed;
        this.expiresAt = expiresAt;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Coupon create(String code, CouponDiscountType discountType, BigDecimal discountValue,
                                Integer maxRedemptions, LocalDateTime expiresAt) {
        Coupon c = new Coupon();
        c.code = code.trim().toUpperCase();
        c.discountType = discountType;
        c.discountValue = discountValue;
        c.maxRedemptions = maxRedemptions;
        c.timesRedeemed = 0;
        c.expiresAt = expiresAt;
        c.active = true;
        return c;
    }

    /** Throws {@link CouponException} if the coupon cannot currently be used. */
    public void assertUsable() {
        if (!active) {
            throw new CouponException("Coupon is not active.");
        }
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            throw new CouponException("Coupon has expired.");
        }
        if (maxRedemptions != null && timesRedeemed >= maxRedemptions) {
            throw new CouponException("Coupon has reached its redemption limit.");
        }
    }

    /** The price after applying this coupon, never below zero. */
    public BigDecimal apply(BigDecimal original) {
        BigDecimal discounted;
        if (discountType == CouponDiscountType.PERCENTAGE) {
            BigDecimal factor = BigDecimal.ONE.subtract(
                    discountValue.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            discounted = original.multiply(factor);
        } else {
            discounted = original.subtract(discountValue);
        }
        if (discounted.compareTo(BigDecimal.ZERO) < 0) {
            discounted = BigDecimal.ZERO;
        }
        return discounted.setScale(2, RoundingMode.HALF_UP);
    }

    public void redeem() {
        this.timesRedeemed++;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public CouponDiscountType getDiscountType() { return discountType; }
    public void setDiscountType(CouponDiscountType discountType) { this.discountType = discountType; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public Integer getMaxRedemptions() { return maxRedemptions; }
    public void setMaxRedemptions(Integer maxRedemptions) { this.maxRedemptions = maxRedemptions; }

    public int getTimesRedeemed() { return timesRedeemed; }
    public void setTimesRedeemed(int timesRedeemed) { this.timesRedeemed = timesRedeemed; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
