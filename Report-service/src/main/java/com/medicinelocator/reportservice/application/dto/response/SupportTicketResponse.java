package com.medicinelocator.reportservice.application.dto.response;

import com.medicinelocator.reportservice.domain.enums.IssueCategory;
import com.medicinelocator.reportservice.domain.enums.ReportStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class SupportTicketResponse {

    private UUID id;
    private UUID reporterId;
    private String reporterRole;
    private IssueCategory category;
    private String subject;
    private String description;
    private ReportStatus status;
    private String adminResponse;
    private UUID resolvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SupportTicketResponse() {
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
