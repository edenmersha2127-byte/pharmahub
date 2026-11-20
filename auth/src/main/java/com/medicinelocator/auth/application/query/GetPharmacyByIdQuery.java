package com.medicinelocator.auth.application.query;

import java.util.UUID;

public class GetPharmacyByIdQuery {

    private final UUID pharmacyId;

    public GetPharmacyByIdQuery(UUID pharmacyId) {
        this.pharmacyId = pharmacyId;
    }

    public UUID getPharmacyId() { return pharmacyId; }
}