package com.medicinelocator.reportservice.application.command;

import com.medicinelocator.reportservice.domain.enums.ReportCategory;

import java.util.UUID;

public class SubmitReportCommand {

    private final UUID reporterId;
    private final UUID pharmacyId;
    private final ReportCategory category;
    private final String description;

    public SubmitReportCommand(UUID reporterId, UUID pharmacyId,
                               ReportCategory category, String description) {
        this.reporterId = reporterId;
        this.pharmacyId = pharmacyId;
        this.category = category;
        this.description = description;
    }

    public UUID getReporterId() { return reporterId; }
    public UUID getPharmacyId() { return pharmacyId; }
    public ReportCategory getCategory() { return category; }
    public String getDescription() { return description; }
}
