package com.medicinelocator.auth.application.command;

import java.util.UUID;

/**
 * A pharmacy sets whether it is currently open for business.
 */
public class SetPharmacyOpenStatusCommand {

    private final UUID pharmacyId;
    private final boolean open;

    public SetPharmacyOpenStatusCommand(UUID pharmacyId, boolean open) {
        this.pharmacyId = pharmacyId;
        this.open = open;
    }

    public UUID getPharmacyId() { return pharmacyId; }

    public boolean isOpen() { return open; }
}
