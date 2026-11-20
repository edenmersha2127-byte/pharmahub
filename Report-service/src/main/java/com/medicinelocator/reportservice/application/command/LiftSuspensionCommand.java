package com.medicinelocator.reportservice.application.command;

import java.util.UUID;

public class LiftSuspensionCommand {

    private final UUID pharmacyId;
    private final UUID adminId;
    private final String actorRole;

    public LiftSuspensionCommand(UUID pharmacyId, UUID adminId, String actorRole) {
        this.pharmacyId = pharmacyId;
        this.adminId = adminId;
        this.actorRole = actorRole;
    }

    public UUID getPharmacyId() { return pharmacyId; }
    public UUID getAdminId() { return adminId; }
    public String getActorRole() { return actorRole; }
}
