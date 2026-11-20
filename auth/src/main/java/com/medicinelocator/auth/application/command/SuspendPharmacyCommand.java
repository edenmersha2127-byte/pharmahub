package com.medicinelocator.auth.application.command;

import java.util.UUID;

public class SuspendPharmacyCommand {

    private final UUID pharmacyId;
    private final int days;

    public SuspendPharmacyCommand(UUID pharmacyId, int days) {
        this.pharmacyId = pharmacyId;
        this.days = days;
    }

    public UUID getPharmacyId() { return pharmacyId; }
    public int getDays() { return days; }
}
