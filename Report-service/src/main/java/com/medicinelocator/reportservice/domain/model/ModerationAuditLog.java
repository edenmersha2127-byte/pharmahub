package com.medicinelocator.reportservice.domain.model;

import com.medicinelocator.reportservice.domain.enums.ModerationAction;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * An immutable record of a single moderation action performed by an admin.
 * Written for every resolve/dismiss/warn/suspend/lift so the super admin can
 * audit what report admins (and admins) have done.
 *
 * Pure domain object: no framework annotations.
 */
public class ModerationAuditLog {

    private UUID id;
    private ModerationAction action;
    private UUID actorId;        // the admin who performed the action
    private String actorRole;    // ADMIN or REPORT_ADMIN (from the gateway header)
    private UUID reportId;       // set for report-scoped actions (nullable)
    private UUID pharmacyId;     // set for pharmacy-scoped actions (nullable)
    private String details;      // free text: note / reason / duration
    private LocalDateTime createdAt;

    public ModerationAuditLog() {
    }

    public ModerationAuditLog(UUID id, ModerationAction action, UUID actorId, String actorRole,
                              UUID reportId, UUID pharmacyId, String details,
                              LocalDateTime createdAt) {
        this.id = id;
        this.action = action;
        this.actorId = actorId;
        this.actorRole = actorRole;
        this.reportId = reportId;
        this.pharmacyId = pharmacyId;
        this.details = details;
        this.createdAt = createdAt;
    }

    /** Factory for a new audit entry (timestamp set at persistence time). */
    public static ModerationAuditLog create(ModerationAction action, UUID actorId, String actorRole,
                                            UUID reportId, UUID pharmacyId, String details) {
        ModerationAuditLog logEntry = new ModerationAuditLog();
        logEntry.action = action;
        logEntry.actorId = actorId;
        logEntry.actorRole = actorRole;
        logEntry.reportId = reportId;
        logEntry.pharmacyId = pharmacyId;
        logEntry.details = details;
        return logEntry;
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
