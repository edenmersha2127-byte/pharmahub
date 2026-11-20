package com.medicinelocator.reportservice.application.dto.response;

import com.medicinelocator.reportservice.domain.enums.ModerationAction;
import com.medicinelocator.reportservice.domain.model.ModerationAuditLog;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuditLogResponse {

    private UUID id;
    private ModerationAction action;
    private UUID actorId;
    private String actorRole;
    private UUID reportId;
    private UUID pharmacyId;
    private String details;
    private LocalDateTime createdAt;

    public AuditLogResponse() {
    }

    public static AuditLogResponse from(ModerationAuditLog logEntry) {
        AuditLogResponse r = new AuditLogResponse();
        r.id = logEntry.getId();
        r.action = logEntry.getAction();
        r.actorId = logEntry.getActorId();
        r.actorRole = logEntry.getActorRole();
        r.reportId = logEntry.getReportId();
        r.pharmacyId = logEntry.getPharmacyId();
        r.details = logEntry.getDetails();
        r.createdAt = logEntry.getCreatedAt();
        return r;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ModerationAction getAction() { return action; }
    public void setAction(ModerationAction action) { this.action = action; }

    public UUID getActorId() { return actorId; }
    public void setActorId(UUID actorId) { this.actorId = actorId; }

    public String getActorRole() { return actorRole; }
    public void setActorRole(String actorRole) { this.actorRole = actorRole; }

    public UUID getReportId() { return reportId; }
    public void setReportId(UUID reportId) { this.reportId = reportId; }

    public UUID getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
