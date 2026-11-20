package com.medicinelocator.prescription.domain.enums;

/**
 * Outcome of verifying a scanned prescription against its issuing hospital.
 */
public enum VerificationStatus {

    /** The prescription exists at the issuing hospital. */
    VALID,

    /** The hospital responded that the prescription does not exist. */
    INVALID,

    /** The extracted hospital ID is not in our integrated-hospitals directory. */
    HOSPITAL_NOT_INTEGRATED,

    /** The hospital ID / prescription ID could not be read from the image. */
    UNIDENTIFIABLE,

    /** The hospital could not be reached / returned an error (fail closed). */
    UNVERIFIABLE
}
