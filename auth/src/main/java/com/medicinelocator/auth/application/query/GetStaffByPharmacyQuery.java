package com.medicinelocator.auth.application.query;

import java.util.UUID;

public class GetStaffByPharmacyQuery {

    private final UUID pharmacyId;

    public GetStaffByPharmacyQuery(UUID pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public UUID getPharmacyId() { return pharmacyId; }
}
