package com.medicinelocator.paymentservice.application.dto.request;

import com.medicinelocator.paymentservice.domain.enums.BillingCycle;
import jakarta.validation.constraints.NotNull;

/**
 * Pharmacy request to start a paid subscription. couponCode is optional.
 */
public class SubscribeRequest {

    @NotNull
    private BillingCycle plan;

    private String couponCode;

    public SubscribeRequest() {
    }

    public BillingCycle getPlan() { return plan; }
    public void setPlan(BillingCycle plan) { this.plan = plan; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
}
