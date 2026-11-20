package com.medicinelocator.reportservice.application.command;

import java.util.UUID;

public class RespondToReportCommand {

    private final UUID reportId;
    private final UUID pharmacyId;
    private final String response;

    public RespondToReportCommand(UUID reportId, UUID pharmacyId, String response) {
        this.reportId = reportId;
        this.pharmacyId = pharmacyId;
        this.response = response;
    }

    public UUID getReportId() { return reportId; }
    public UUID getPharmacyId() { return pharmacyId; }
    public String getResponse() { return response; }
}
