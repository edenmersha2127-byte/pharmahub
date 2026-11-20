package com.medicinelocator.auth.application.query;

import com.medicinelocator.auth.domain.enums.PharmacyStatus;

public class GetPharmaciesQuery {

    private final PharmacyStatus statusFilter;
    private final int page;
    private final int size;

    public GetPharmaciesQuery(PharmacyStatus statusFilter, int page, int size) {
        this.statusFilter = statusFilter;
        this.page = page;
        this.size = size;
    }

    public PharmacyStatus getStatusFilter() { return statusFilter; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}