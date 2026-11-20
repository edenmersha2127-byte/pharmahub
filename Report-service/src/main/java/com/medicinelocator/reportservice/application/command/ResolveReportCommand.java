package com.medicinelocator.reportservice.application.command;

import java.util.UUID;

public class ResolveReportCommand {

    private final UUID reportId;
    private final UUID adminId;
    private final String actorRole;
    private final String note;

    public ResolveReportCommand(UUID reportId, UUID adminId, String actorRole, String note) {
        this.reportId = reportId;
        this.adminId = adminId;
        this.actorRole = actorRole;
        this.note = note;
    }

    public UUID getReportId() { return reportId; }
    public UUID getAdminId() { return adminId; }
    public String getActorRole() { return actorRole; }
    public String getNote() { return note; }
}
