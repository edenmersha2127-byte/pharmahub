package com.medicinelocator.auth.application.command;

import java.time.LocalDateTime;
import java.util.UUID;

public class SetPharmacySubscriptionCommand {

    private final UUID pharmacyId;
    private final String status;              // TRIAL | ACTIVE | EXPIRED
    private final LocalDateTime subscribedUntil;

    public SetPharmacySubscriptionCommand(UUID pharmacyId, String status, LocalDateTime subscribedUntil) {
        this.pharmacyId = pharmacyId;
        this.status = status;
        this.subscribedUntil = subscribedUntil;
    }

    public UUID getPharmacyId() { return pharmacyId; }
    public String getStatus() { return status; }
    public LocalDateTime getSubscribedUntil() { return subscribedUntil; }
}
