package com.medicinelocator.paymentservice.application.command;

import java.util.UUID;

public class StartTrialCommand {

    private final UUID pharmacyId;

    public StartTrialCommand(UUID pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public UUID getPharmacyId() { return pharmacyId; }
}
