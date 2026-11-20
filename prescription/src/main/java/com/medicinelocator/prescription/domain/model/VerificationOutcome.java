package com.medicinelocator.prescription.domain.model;

import com.medicinelocator.prescription.domain.enums.VerificationStatus;

/**
 * Result of the verification step. Only {@link VerificationStatus#VALID}
 * allows the medicine-extraction + pharmacy-search flow to continue.
 */
public class VerificationOutcome {

    private final VerificationStatus status;
    private final String message;
    private final String hospitalId;
    private final String prescriptionId;

    public VerificationOutcome(VerificationStatus status, String message,
                               String hospitalId, String prescriptionId) {
        this.status = status;
        this.message = message;
        this.hospitalId = hospitalId;
        this.prescriptionId = prescriptionId;
    }

    public boolean isValid() {
        return status == VerificationStatus.VALID;
    }

    public static VerificationOutcome valid(String hospitalId, String prescriptionId) {
        return new VerificationOutcome(VerificationStatus.VALID,
                "Prescription verified with the issuing hospital", hospitalId, prescriptionId);
    }

    public static VerificationOutcome invalid(String hospitalId, String prescriptionId) {
        return new VerificationOutcome(VerificationStatus.INVALID,
                "Invalid prescription — not found at the issuing hospital", hospitalId, prescriptionId);
    }

    public static VerificationOutcome notIntegrated(String hospitalId) {
        return new VerificationOutcome(VerificationStatus.HOSPITAL_NOT_INTEGRATED,
                "Not integrated hospital ID: " + hospitalId, hospitalId, null);
    }

    public static VerificationOutcome unidentifiable() {
        return new VerificationOutcome(VerificationStatus.UNIDENTIFIABLE,
                "Could not read a hospital ID and prescription ID from the image", null, null);
    }

    public static VerificationOutcome unverifiable(String hospitalId, String prescriptionId, String reason) {
        return new VerificationOutcome(VerificationStatus.UNVERIFIABLE,
                "Could not reach the issuing hospital to verify: " + reason, hospitalId, prescriptionId);
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }
}
