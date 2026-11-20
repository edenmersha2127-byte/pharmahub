package com.medicinelocator.prescription.domain.model;

/**
 * The two identifiers printed on a prescription that let us verify it:
 * the issuing hospital's ID and the prescription's own ID.
 */
public class PrescriptionIdentity {

    private final String hospitalId;
    private final String prescriptionId;

    public PrescriptionIdentity(String hospitalId, String prescriptionId) {
        this.hospitalId = hospitalId;
        this.prescriptionId = prescriptionId;
    }

    public static PrescriptionIdentity empty() {
        return new PrescriptionIdentity(null, null);
    }

    /** True only when both identifiers were successfully read. */
    public boolean isPresent() {
        return hospitalId != null && !hospitalId.isBlank()
                && prescriptionId != null && !prescriptionId.isBlank();
    }

    public String getHospitalId() {
        return hospitalId;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }
}
