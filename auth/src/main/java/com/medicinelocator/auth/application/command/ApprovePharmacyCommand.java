package com.medicinelocator.auth.application.command;

import java.util.UUID;

public class ApprovePharmacyCommand {

    private final UUID pharmacyId;

    public ApprovePharmacyCommand(UUID pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public UUID getPharmacyId() { return pharmacyId; }
}