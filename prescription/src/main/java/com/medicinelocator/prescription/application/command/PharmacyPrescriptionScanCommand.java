package com.medicinelocator.prescription.application.command;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * A pharmacy scanning a walk-in customer's prescription to verify it and load
 * the prescribed medicines into the POS.
 */
public class PharmacyPrescriptionScanCommand {

    private final UUID pharmacyId;
    private final MultipartFile file;

    public PharmacyPrescriptionScanCommand(UUID pharmacyId, MultipartFile file) {
        this.pharmacyId = pharmacyId;
        this.file = file;
    }

    public UUID getPharmacyId() { return pharmacyId; }
    public MultipartFile getFile() { return file; }
}
