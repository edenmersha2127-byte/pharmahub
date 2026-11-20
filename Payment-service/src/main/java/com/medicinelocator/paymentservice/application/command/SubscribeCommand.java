package com.medicinelocator.paymentservice.application.command;

import com.medicinelocator.paymentservice.domain.enums.BillingCycle;

import java.util.UUID;

public class SubscribeCommand {

    private final UUID pharmacyId;
    private final BillingCycle plan;
    private final String couponCode;   // nullable

    public SubscribeCommand(UUID pharmacyId, BillingCycle plan, String couponCode) {
        this.pharmacyId = pharmacyId;
        this.plan = plan;
        this.couponCode = couponCode;
    }

    public UUID getPharmacyId() { return pharmacyId; }
    public BillingCycle getPlan() { return plan; }
    public String getCouponCode() { return couponCode; }
}
