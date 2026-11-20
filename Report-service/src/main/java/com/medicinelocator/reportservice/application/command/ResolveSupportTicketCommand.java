package com.medicinelocator.reportservice.application.command;

import java.util.UUID;

public class ResolveSupportTicketCommand {

    private final UUID ticketId;
    private final UUID adminId;
    private final String actorRole;
    private final String note;

    public ResolveSupportTicketCommand(UUID ticketId, UUID adminId, String actorRole, String note) {
        this.ticketId = ticketId;
        this.adminId = adminId;
        this.actorRole = actorRole;
        this.note = note;
    }

    public UUID getTicketId() { return ticketId; }
    public UUID getAdminId() { return adminId; }
    public String getActorRole() { return actorRole; }
    public String getNote() { return note; }
}
