package com.medicinelocator.auth.application.command;

import java.util.UUID;

public class LiftPharmacySuspensionCommand {

    private final UUID pharmacyId;

    public LiftPharmacySuspensionCommand(UUID pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public UUID getPharmacyId() { return pharmacyId; }
}
