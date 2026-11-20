package com.medicinelocator.reportservice.application.command;

import java.util.UUID;

public class SuspendPharmacyCommand {

    private final UUID pharmacyId;
    private final UUID adminId;
    private final String actorRole;
    private final Integer days;   // null -> use configured default
    private final String reason;

    public SuspendPharmacyCommand(UUID pharmacyId, UUID adminId, String actorRole,
                                  Integer days, String reason) {
        this.pharmacyId = pharmacyId;
        this.adminId = adminId;
        this.actorRole = actorRole;
        this.days = days;
        this.reason = reason;
    }

    public UUID getPharmacyId() { return pharmacyId; }
    public UUID getAdminId() { return adminId; }
    public String getActorRole() { return actorRole; }
    public Integer getDays() { return days; }
    public String getReason() { return reason; }
}
