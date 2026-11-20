package com.medicinelocator.reportservice.application.dto.response;

import java.util.UUID;

/**
 * Lightweight pharmacy record fetched from the Auth Service internal endpoint,
 * used to enrich reports (pharmacy name) and to target notifications.
 *
 * Field names mirror the Auth Service payload so Jackson binds them directly;
 * unknown extra fields are ignored.
 */
public class PharmacySummaryResponse {

    private UUID pharmacyId;
    private String pharmacyName;
    private String email;

    public PharmacySummaryResponse() {
    }

    public PharmacySummaryResponse(UUID pharmacyId, String pharmacyName, String email) {
        this.pharmacyId = pharmacyId;
        this.pharmacyName = pharmacyName;
        this.email = email;
    }

    public UUID getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }

    public String getPharmacyName() { return pharmacyName; }
    public void setPharmacyName(String pharmacyName) { this.pharmacyName = pharmacyName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
