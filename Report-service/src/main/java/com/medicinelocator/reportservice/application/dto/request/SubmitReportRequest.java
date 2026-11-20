package com.medicinelocator.reportservice.application.dto.request;

import com.medicinelocator.reportservice.domain.enums.ReportCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Payload a customer sends to report a pharmacy. The reporter's identity comes
 * from the gateway X-User-Id header, not the body.
 */
public class SubmitReportRequest {

    @NotNull
    private UUID pharmacyId;

    @NotNull
    private ReportCategory category;

    @NotBlank
    @Size(max = 2000)
    private String description;

    public SubmitReportRequest() {
    }

    public UUID getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }

    public ReportCategory getCategory() { return category; }
    public void setCategory(ReportCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
