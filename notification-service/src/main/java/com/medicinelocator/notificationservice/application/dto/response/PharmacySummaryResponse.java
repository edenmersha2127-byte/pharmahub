package com.medicinelocator.notificationservice.application.dto.response;

import java.util.UUID;

/**
 * Lightweight pharmacy record fetched from the Auth Service, used to build
 * the recipient list for pharmacy-wide notifications.
 *
 * Field names mirror the Auth Service internal endpoint payload so Jackson
 * binds them directly.
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
