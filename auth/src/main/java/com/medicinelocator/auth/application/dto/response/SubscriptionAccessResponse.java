package com.medicinelocator.auth.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Whether a pharmacy currently has subscription access. Consumed by the
 * Inventory Service to gate inventory changes.
 */
public class SubscriptionAccessResponse {

    private UUID pharmacyId;
    private boolean active;
    private String status;
    private LocalDateTime subscribedUntil;

    public SubscriptionAccessResponse() {
    }

    public SubscriptionAccessResponse(UUID pharmacyId, boolean active,
                                      String status, LocalDateTime subscribedUntil) {
        this.pharmacyId = pharmacyId;
        this.active = active;
        this.status = status;
        this.subscribedUntil = subscribedUntil;
    }

    public UUID getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubscribedUntil() { return subscribedUntil; }
    public void setSubscribedUntil(LocalDateTime subscribedUntil) { this.subscribedUntil = subscribedUntil; }
}
