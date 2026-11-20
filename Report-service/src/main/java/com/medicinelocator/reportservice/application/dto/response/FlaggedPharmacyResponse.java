package com.medicinelocator.reportservice.application.dto.response;

import java.util.UUID;

/**
 * A row in the admin "flagged pharmacies" dashboard: a pharmacy with an
 * elevated number of unresolved reports, enriched with its name from Auth.
 */
public class FlaggedPharmacyResponse {

    private UUID pharmacyId;
    private String pharmacyName;
    private long unresolvedReports;

    public FlaggedPharmacyResponse() {
    }

    public FlaggedPharmacyResponse(UUID pharmacyId, String pharmacyName, long unresolvedReports) {
        this.pharmacyId = pharmacyId;
        this.pharmacyName = pharmacyName;
        this.unresolvedReports = unresolvedReports;
    }

    public UUID getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }

    public String getPharmacyName() { return pharmacyName; }
    public void setPharmacyName(String pharmacyName) { this.pharmacyName = pharmacyName; }

    public long getUnresolvedReports() { return unresolvedReports; }
    public void setUnresolvedReports(long unresolvedReports) { this.unresolvedReports = unresolvedReports; }
}
