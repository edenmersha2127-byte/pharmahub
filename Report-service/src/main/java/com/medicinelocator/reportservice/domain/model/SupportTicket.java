package com.medicinelocator.reportservice.domain.model;

import com.medicinelocator.reportservice.domain.enums.IssueCategory;
import com.medicinelocator.reportservice.domain.enums.ReportStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A software/experience issue any authenticated user (customer, pharmacy,
 * pharmacy staff or admin) raises with the platform admins — e.g. "search not
 * working", "upload failed", a bug or a feature request.
 *
 * Distinct from {@link Report}: there is no pharmacy on the other side, so no
 * right-to-respond, warn or suspend. An admin simply resolves or dismisses it.
 *
 * Pure domain object: no framework annotations. Persistence and transport
 * mapping live in the infrastructure and application layers respectively.
 */
public class SupportTicket {

    private UUID id;
    private UUID reporterId;        // the user who raised the ticket
    private String reporterRole;    // CUSTOMER / PHARMACY / PHARMACY_STAFF / ADMIN
    private IssueCategory category;
    private String subject;
    private String description;
    private ReportStatus status;
    private String adminResponse;   // admin's note when resolving/dismissing
    private UUID resolvedBy;        // admin who closed it
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SupportTicket() {
    }

    public SupportTicket(UUID id, UUID reporterId, String reporterRole, IssueCategory category,
                         String subject, String description, ReportStatus status,
                         String adminResponse, UUID resolvedBy,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.reporterId = reporterId;
        this.reporterRole = reporterRole;
        this.category = category;
        this.subject = subject;
        this.description = description;
        this.status = status;
        this.adminResponse = adminResponse;
        this.resolvedBy = resolvedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Factory for a brand-new, pending ticket. */
    public static SupportTicket create(UUID reporterId, String reporterRole,
                                       IssueCategory category, String subject, String description) {
        SupportTicket t = new SupportTicket();
        t.reporterId = reporterId;
        t.reporterRole = reporterRole;
        t.category = category;
        t.subject = subject;
        t.description = description;
        t.status = ReportStatus.PENDING;
        return t;
    }

    /** Admin marks the ticket resolved (issue addressed). */
    public void resolve(UUID adminId, String note) {
        this.status = ReportStatus.RESOLVED;
        this.resolvedBy = adminId;
        this.adminResponse = note;
        this.updatedAt = LocalDateTime.now();
    }

    /** Admin dismisses the ticket (not actionable / not an issue). */
    public void dismiss(UUID adminId, String note) {
        this.status = ReportStatus.DISMISSED;
        this.resolvedBy = adminId;
        this.adminResponse = note;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isUnresolved() {
        return status != null && status.isUnresolved();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getReporterId() { return reporterId; }
    public void setReporterId(UUID reporterId) { this.reporterId = reporterId; }

    public String getReporterRole() { return reporterRole; }
    public void setReporterRole(String reporterRole) { this.reporterRole = reporterRole; }

    public IssueCategory getCategory() { return category; }
    public void setCategory(IssueCategory category) { this.category = category; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }

    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }

    public UUID getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(UUID resolvedBy) { this.resolvedBy = resolvedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
