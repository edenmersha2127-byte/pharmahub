package com.medicinelocator.prescription.application.dto.response;

import java.util.List;

/**
 * Result of a pharmacy dispense-scan. Three outcomes drive the UI:
 *  - verified == true  → green: confirmed with the issuing hospital.
 *  - canDispense == true && verified == false → amber: could not be verified
 *    (paper script / non-integrated hospital / unreadable) — the pharmacist
 *    decides whether to proceed.
 *  - canDispense == false → red: the issuing hospital says it is INVALID.
 * {@code medicines} carries the extracted items resolved to the pharmacy's own
 * catalogue, ready to load straight into the POS cart.
 */
public record PharmacyScanResponse(
        String verificationStatus,
        boolean verified,
        boolean canDispense,
        String verificationMessage,
        List<String> extractedMedicines,
        List<ResolvedMedicineResponse> medicines,
        String message
) {
}
