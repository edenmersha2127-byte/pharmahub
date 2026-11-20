package com.medicinelocator.reportservice.domain.model;

import com.medicinelocator.reportservice.domain.enums.ReportCategory;
import com.medicinelocator.reportservice.domain.enums.ReportStatus;
import com.medicinelocator.reportservice.domain.exception.ReportAccessDeniedException;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A complaint a customer files against a pharmacy (closed when advertised open,
 * medicine unavailable, wrong price, ...).
 *
 * Pure domain object: no framework annotations. Persistence and transport
 * mapping live in the infrastructure and application layers respectively.
 */
public class Report {

    private UUID id;
    private UUID reporterId;      // the customer who filed it
    private UUID pharmacyId;      // the reported pharmacy
    private ReportCategory category;
    private String description;
    private ReportStatus status;
    private String pharmacyResponse;  // pharmacy's reply (right-to-respond)
    private String resolutionNote;    // admin's note when resolving/dismissing
    private UUID resolvedBy;          // admin who closed it
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Report() {
    }

    public Report(UUID id, UUID reporterId, UUID pharmacyId, ReportCategory category,
                  String description, ReportStatus status, String pharmacyResponse,
                  String resolutionNote, UUID resolvedBy,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.reporterId = reporterId;
        this.pharmacyId = pharmacyId;
        this.category = category;
        this.description = description;
        this.status = status;
        this.pharmacyResponse = pharmacyResponse;
        this.resolutionNote = resolutionNote;
        this.resolvedBy = resolvedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Factory for a brand-new, pending report. */
    public static Report create(UUID reporterId, UUID pharmacyId,
                                ReportCategory category, String description) {
        Report r = new Report();
        r.reporterId = reporterId;
        r.pharmacyId = pharmacyId;
        r.category = category;
        r.description = description;
        r.status = ReportStatus.PENDING;
        return r;
    }

    /**
     * The pharmacy replies to the complaint. Moves an open report into review
     * so an admin can weigh both sides. Only the owning pharmacy may respond.
     */
    public void respondAsPharmacy(UUID pharmacyId, String response) {
        if (!this.pharmacyId.equals(pharmacyId)) {
            throw new ReportAccessDeniedException("This report was not filed against your pharmacy.");
        }
        this.pharmacyResponse = response;
        if (status.isUnresolved()) {
            this.status = ReportStatus.UNDER_REVIEW;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /** Admin marks the report resolved (issue acted on / addressed). */
    public void resolve(UUID adminId, String note) {
        this.status = ReportStatus.RESOLVED;
        this.resolvedBy = adminId;
        this.resolutionNote = note;
        this.updatedAt = LocalDateTime.now();
    }

    /** Admin dismisses the report as unfounded. */
    public void dismiss(UUID adminId, String note) {
        this.status = ReportStatus.DISMISSED;
        this.resolvedBy = adminId;
        this.resolutionNote = note;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isUnresolved() {
        return status != null && status.isUnresolved();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getReporterId() { return reporterId; }
    public void setReporterId(UUID reporterId) { this.reporterId = reporterId; }

    public UUID getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }

    public ReportCategory getCategory() { return category; }
    public void setCategory(ReportCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }

    public String getPharmacyResponse() { return pharmacyResponse; }
    public void setPharmacyResponse(String pharmacyResponse) { this.pharmacyResponse = pharmacyResponse; }

    public String getResolutionNote() { return resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }

    public UUID getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(UUID resolvedBy) { this.resolvedBy = resolvedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
