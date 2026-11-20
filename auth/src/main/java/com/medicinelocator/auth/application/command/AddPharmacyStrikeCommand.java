package com.medicinelocator.auth.application.command;

import java.util.UUID;

public class AddPharmacyStrikeCommand {

    private final UUID pharmacyId;

    public AddPharmacyStrikeCommand(UUID pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public UUID getPharmacyId() { return pharmacyId; }
}
