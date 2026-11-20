package com.medicinelocator.prescription.application.service;

import com.medicinelocator.prescription.domain.model.HospitalConnection;
import com.medicinelocator.prescription.domain.model.VerificationOutcome;

/**
 * Port for calling an integrated hospital's public verification endpoint.
 */
public interface HospitalVerificationClient {

    VerificationOutcome verify(HospitalConnection connection, String prescriptionId);
}
