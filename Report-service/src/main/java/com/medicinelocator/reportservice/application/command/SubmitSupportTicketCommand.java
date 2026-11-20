package com.medicinelocator.reportservice.application.command;

import com.medicinelocator.reportservice.domain.enums.IssueCategory;

import java.util.UUID;

public class SubmitSupportTicketCommand {

    private final UUID reporterId;
    private final String reporterRole;
    private final IssueCategory category;
    private final String subject;
    private final String description;

    public SubmitSupportTicketCommand(UUID reporterId, String reporterRole,
                                      IssueCategory category, String subject, String description) {
        this.reporterId = reporterId;
        this.reporterRole = reporterRole;
        this.category = category;
        this.subject = subject;
        this.description = description;
    }

    public UUID getReporterId() { return reporterId; }
    public String getReporterRole() { return reporterRole; }
    public IssueCategory getCategory() { return category; }
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
}
