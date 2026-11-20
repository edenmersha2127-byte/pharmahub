package com.medicinelocator.reportservice.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Admin request to temporarily suspend a pharmacy.
 *
 * {@code days} is optional; when null the service applies the configured
 * default (3 days). Bounded to keep suspensions sane.
 */
public class SuspendPharmacyRequest {

    @Min(1)
    @Max(365)
    private Integer days;

    @Size(max = 2000)
    private String reason;

    public SuspendPharmacyRequest() {
    }

    public Integer getDays() { return days; }
    public void setDays(Integer days) { this.days = days; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
