package com.medicinelocator.reportservice.application.query;

import java.util.UUID;

public class GetReportsForPharmacyQuery {

    private final UUID pharmacyId;
    private final int page;
    private final int size;

    public GetReportsForPharmacyQuery(UUID pharmacyId, int page, int size) {
        this.pharmacyId = pharmacyId;
        this.page = page;
        this.size = size;
    }

    public UUID getPharmacyId() { return pharmacyId; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}
