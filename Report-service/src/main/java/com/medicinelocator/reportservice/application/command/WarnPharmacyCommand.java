package com.medicinelocator.reportservice.application.command;

import java.util.UUID;

public class WarnPharmacyCommand {

    private final UUID pharmacyId;
    private final UUID adminId;
    private final String actorRole;
    private final String reason;

    public WarnPharmacyCommand(UUID pharmacyId, UUID adminId, String actorRole, String reason) {
        this.pharmacyId = pharmacyId;
        this.adminId = adminId;
        this.actorRole = actorRole;
        this.reason = reason;
    }

    public UUID getPharmacyId() { return pharmacyId; }
    public UUID getAdminId() { return adminId; }
    public String getActorRole() { return actorRole; }
    public String getReason() { return reason; }
}
