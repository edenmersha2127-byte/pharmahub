package com.medicinelocator.paymentservice.domain.model;

import com.medicinelocator.paymentservice.domain.enums.BillingCycle;
import com.medicinelocator.paymentservice.domain.enums.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A pharmacy's subscription period (trial or paid). Pure domain object.
 */
public class Subscription {

    private UUID id;
    private UUID pharmacyId;
    private BillingCycle plan;   // null for a trial
    private SubscriptionStatus status;
    private BigDecimal amount;   // charged amount (0/null for trial)
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean reminderSent;   // expiry reminder already sent for this period
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Subscription() {
    }

    public Subscription(UUID id, UUID pharmacyId, BillingCycle plan, SubscriptionStatus status,
                        BigDecimal amount, LocalDateTime startDate, LocalDateTime endDate,
                        boolean reminderSent, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.pharmacyId = pharmacyId;
        this.plan = plan;
        this.status = status;
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reminderSent = reminderSent;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** A free trial starting now for the given number of days. */
    public static Subscription createTrial(UUID pharmacyId, int trialDays) {
        Subscription s = new Subscription();
        s.pharmacyId = pharmacyId;
        s.plan = null;
        s.status = SubscriptionStatus.TRIAL;
        s.amount = BigDecimal.ZERO;
        s.startDate = LocalDateTime.now();
        s.endDate = s.startDate.plusDays(trialDays);
        return s;
    }

    /** A paid subscription awaiting payment confirmation. */
    public static Subscription createPending(UUID pharmacyId, BillingCycle plan, BigDecimal amount) {
        Subscription s = new Subscription();
        s.pharmacyId = pharmacyId;
        s.plan = plan;
        s.status = SubscriptionStatus.PENDING;
        s.amount = amount;
        s.startDate = LocalDateTime.now();
        return s;
    }

    /**
     * Activate a paid subscription for {@code durationDays}. If the pharmacy is
     * still within an existing paid period, extend from that period's end so no
     * paid time is lost.
     */
    public void activate(int durationDays, LocalDateTime currentAccessUntil) {
        LocalDateTime base = (currentAccessUntil != null && currentAccessUntil.isAfter(LocalDateTime.now()))
                ? currentAccessUntil
                : LocalDateTime.now();
        this.status = SubscriptionStatus.ACTIVE;
        this.endDate = base.plusDays(durationDays);
        this.reminderSent = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markReminderSent() {
        this.reminderSent = true;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean grantsAccess() {
        return status != null && status.grantsAccess()
                && endDate != null && endDate.isAfter(LocalDateTime.now());
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }

    public BillingCycle getPlan() { return plan; }
    public void setPlan(BillingCycle plan) { this.plan = plan; }

    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public boolean isReminderSent() { return reminderSent; }
    public void setReminderSent(boolean reminderSent) { this.reminderSent = reminderSent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
