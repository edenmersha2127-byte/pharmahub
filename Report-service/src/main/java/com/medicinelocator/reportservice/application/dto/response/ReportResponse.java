package com.medicinelocator.reportservice.application.dto.response;

import com.medicinelocator.reportservice.domain.enums.ReportCategory;
import com.medicinelocator.reportservice.domain.enums.ReportStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReportResponse {

    private UUID id;
    private UUID reporterId;
    private UUID pharmacyId;
    private ReportCategory category;
    private String description;
    private ReportStatus status;
    private String pharmacyResponse;
    private String resolutionNote;
    private UUID resolvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Total lifetime reports against this pharmacy (all statuses). Populated in
    // admin views so the admin can gauge repeat offenders; null elsewhere.
    private Long pharmacyReportCount;

    public ReportResponse() {
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

    public Long getPharmacyReportCount() { return pharmacyReportCount; }
    public void setPharmacyReportCount(Long pharmacyReportCount) { this.pharmacyReportCount = pharmacyReportCount; }
}
