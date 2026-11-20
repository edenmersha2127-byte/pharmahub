package com.medicinelocator.paymentservice.application.dto.request;

import com.medicinelocator.paymentservice.domain.enums.CouponDiscountType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Super-admin request to create a coupon code to share with pharmacies.
 */
public class CreateCouponRequest {

    @NotBlank
    private String code;

    @NotNull
    private CouponDiscountType discountType;

    @NotNull
    @Positive
    private BigDecimal discountValue;

    @Min(1)
    private Integer maxRedemptions;   // null = unlimited

    private LocalDateTime expiresAt;  // null = never expires

    public CreateCouponRequest() {
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public CouponDiscountType getDiscountType() { return discountType; }
    public void setDiscountType(CouponDiscountType discountType) { this.discountType = discountType; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public Integer getMaxRedemptions() { return maxRedemptions; }
    public void setMaxRedemptions(Integer maxRedemptions) { this.maxRedemptions = maxRedemptions; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
