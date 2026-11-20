package com.medicinelocator.auth.application.command;

import java.util.UUID;

public class RejectPharmacyCommand {

    private final UUID pharmacyId;

    public RejectPharmacyCommand(UUID pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public UUID getPharmacyId() { return pharmacyId; }
}