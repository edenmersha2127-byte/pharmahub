package com.medicinelocator.auth.application.dto.response;

import java.util.UUID;

/**
 * Lightweight pharmacy record returned by the internal directory endpoint.
 * Consumed by the Notification Service to resolve the audience of
 * pharmacy-wide notifications (e.g. the nightly medicine-update reminder).
 */
public class PharmacySummaryResponse {

    private UUID pharmacyId;
    private String pharmacyName;
    private String email;
    private String status;   // PENDING | APPROVED | REJECTED

    public PharmacySummaryResponse() {
    }

    public PharmacySummaryResponse(UUID pharmacyId, String pharmacyName,
                                   String email, String status) {
        this.pharmacyId = pharmacyId;
        this.pharmacyName = pharmacyName;
        this.email = email;
        this.status = status;
    }

    public UUID getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }

    public String getPharmacyName() { return pharmacyName; }
    public void setPharmacyName(String pharmacyName) { this.pharmacyName = pharmacyName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
