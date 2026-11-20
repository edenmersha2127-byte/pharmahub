package com.medicinelocator.auth.application.query;

import java.util.UUID;

public class GetPharmacyProfileQuery {

    private final UUID pharmacyId;

    public GetPharmacyProfileQuery(UUID pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public UUID getPharmacyId() { return pharmacyId; }
}