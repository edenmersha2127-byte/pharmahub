package com.medicinelocator.reportservice.application.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Optional reason an admin gives when issuing a formal warning (strike).
 */
public class WarnPharmacyRequest {

    @Size(max = 2000)
    private String reason;

    public WarnPharmacyRequest() {
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
