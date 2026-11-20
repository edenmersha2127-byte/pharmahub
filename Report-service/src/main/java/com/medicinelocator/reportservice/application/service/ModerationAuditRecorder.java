package com.medicinelocator.reportservice.application.service;

import com.medicinelocator.reportservice.domain.enums.ModerationAction;
import com.medicinelocator.reportservice.domain.model.ModerationAuditLog;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Writes an audit-log entry for every moderation action. Called by the
 * moderation handlers so there is a permanent, queryable record of who did
 * what (used by the super admin to oversee report admins).
 */
@Component
public class ModerationAuditRecorder {

    private final ModerationAuditLogRepository repository;

    public ModerationAuditRecorder(ModerationAuditLogRepository repository) {
        this.repository = repository;
    }

    public void recordResolve(UUID actorId, String actorRole, UUID reportId,
                              UUID pharmacyId, String note) {
        repository.save(ModerationAuditLog.create(
                ModerationAction.RESOLVE_REPORT, actorId, actorRole, reportId, pharmacyId,
                note != null ? "note: " + note : null));
    }

    public void recordDismiss(UUID actorId, String actorRole, UUID reportId,
                              UUID pharmacyId, String note) {
        repository.save(ModerationAuditLog.create(
                ModerationAction.DISMISS_REPORT, actorId, actorRole, reportId, pharmacyId,
                note != null ? "note: " + note : null));
    }

    public void recordWarn(UUID actorId, String actorRole, UUID pharmacyId, String reason) {
        repository.save(ModerationAuditLog.create(
                ModerationAction.WARN_PHARMACY, actorId, actorRole, null, pharmacyId,
                reason != null ? "reason: " + reason : null));
    }

    public void recordSuspend(UUID actorId, String actorRole, UUID pharmacyId,
                              int days, String reason) {
        String details = "days: " + days + (reason != null ? "; reason: " + reason : "");
        repository.save(ModerationAuditLog.create(
                ModerationAction.SUSPEND_PHARMACY, actorId, actorRole, null, pharmacyId, details));
    }

    public void recordLift(UUID actorId, String actorRole, UUID pharmacyId) {
        repository.save(ModerationAuditLog.create(
                ModerationAction.LIFT_SUSPENSION, actorId, actorRole, null, pharmacyId, null));
    }
}
