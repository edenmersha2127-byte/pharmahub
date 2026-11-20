package com.medicinelocator.paymentservice.domain.model;

import com.medicinelocator.paymentservice.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A single payment attempt against the gateway (Chapa). Pure domain object.
 */
public class Payment {

    private UUID id;
    private UUID subscriptionId;
    private UUID pharmacyId;
    private String txRef;        // our unique reference sent to Chapa
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String couponCode;   // applied coupon, if any
    private String checkoutUrl;  // Chapa hosted checkout link
    private String rawPayload;   // last provider payload, for reconciliation
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment() {
    }

    public Payment(UUID id, UUID subscriptionId, UUID pharmacyId, String txRef, BigDecimal amount,
                   String currency, PaymentStatus status, String couponCode, String checkoutUrl,
                   String rawPayload, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.subscriptionId = subscriptionId;
        this.pharmacyId = pharmacyId;
        this.txRef = txRef;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.couponCode = couponCode;
        this.checkoutUrl = checkoutUrl;
        this.rawPayload = rawPayload;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Payment createPending(UUID subscriptionId, UUID pharmacyId, String txRef,
                                        BigDecimal amount, String currency, String couponCode) {
        Payment p = new Payment();
        p.subscriptionId = subscriptionId;
        p.pharmacyId = pharmacyId;
        p.txRef = txRef;
        p.amount = amount;
        p.currency = currency;
        p.couponCode = couponCode;
        p.status = PaymentStatus.PENDING;
        return p;
    }

    public void markSuccess(String rawPayload) {
        this.status = PaymentStatus.SUCCESS;
        this.rawPayload = rawPayload;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed(String rawPayload) {
        this.status = PaymentStatus.FAILED;
        this.rawPayload = rawPayload;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isSuccessful() {
        return status == PaymentStatus.SUCCESS;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(UUID subscriptionId) { this.subscriptionId = subscriptionId; }

    public UUID getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }

    public String getTxRef() { return txRef; }
    public void setTxRef(String txRef) { this.txRef = txRef; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }

    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
