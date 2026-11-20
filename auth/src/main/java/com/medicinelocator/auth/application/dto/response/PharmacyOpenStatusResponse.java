package com.medicinelocator.auth.application.dto.response;

import java.util.UUID;


public class PharmacyOpenStatusResponse {

    private UUID pharmacyId;
    private boolean open;
    private String message;

    public PharmacyOpenStatusResponse() {
    }

    public PharmacyOpenStatusResponse(UUID pharmacyId, boolean open, String message) {
        this.pharmacyId = pharmacyId;
        this.open = open;
        this.message = message;
    }

    public UUID getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
