package com.medicinelocator.prescription.application.service;

import com.medicinelocator.prescription.domain.model.PrescriptionIdentity;
import org.springframework.web.multipart.MultipartFile;

/**
 * Reads the hospital ID + prescription ID from a prescription image.
 * Independent of medicine extraction (a separate pass), so the existing
 * medicine-extraction flow is never affected.
 */
public interface PrescriptionIdentityExtractor {

    PrescriptionIdentity extractIdentity(MultipartFile file);
}
