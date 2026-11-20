package com.medicinelocator.auth.application.command;

import java.util.UUID;

public class SetStaffActiveCommand {

    private final UUID pharmacyId;
    private final UUID staffId;
    private final boolean active;

    public SetStaffActiveCommand(UUID pharmacyId, UUID staffId, boolean active) {
        this.pharmacyId = pharmacyId;
        this.staffId = staffId;
        this.active = active;
    }

    public UUID getPharmacyId() { return pharmacyId; }
    public UUID getStaffId() { return staffId; }
    public boolean isActive() { return active; }
}
