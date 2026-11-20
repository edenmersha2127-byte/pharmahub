package com.medicinelocator.prescription.presentation.controller;

import com.medicinelocator.prescription.application.command.PharmacyPrescriptionScanCommand;
import com.medicinelocator.prescription.application.command.handler.PharmacyPrescriptionScanHandler;
import com.medicinelocator.prescription.application.dto.response.PharmacyScanResponse;
import com.medicinelocator.prescription.domain.exception.InvalidPrescriptionException;
import com.medicinelocator.prescription.infrastructure.security.CurrentUser;
import com.medicinelocator.prescription.infrastructure.security.CurrentUserProvider;
import com.medicinelocator.prescription.infrastructure.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Pharmacy-facing prescription flow: a pharmacist scans a walk-in customer's
 * prescription; the system verifies it against the issuing hospital, extracts
 * the prescribed medicines, and resolves them against the pharmacy's own
 * catalogue so they can be sent straight to the POS.
 */
@RestController
@RequestMapping("/api/prescriptions/pharmacy")
public class PharmacyPrescriptionController {

    private static final Logger log = LoggerFactory.getLogger(PharmacyPrescriptionController.class);

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/jpg", "application/pdf"
    );

    private final PharmacyPrescriptionScanHandler scanHandler;
    private final CurrentUserProvider currentUserProvider;
    private final SecurityUtils securityUtils;

    public PharmacyPrescriptionController(PharmacyPrescriptionScanHandler scanHandler,
                                          CurrentUserProvider currentUserProvider,
                                          SecurityUtils securityUtils) {
        this.scanHandler = scanHandler;
        this.currentUserProvider = currentUserProvider;
        this.securityUtils = securityUtils;
    }

    @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PharmacyScanResponse> scan(@RequestParam("file") MultipartFile file) {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        UUID pharmacyId = securityUtils.requirePharmacyContext(currentUser);

        validateFileContentType(file);

        MDC.put("pharmacyId", pharmacyId.toString());
        log.info("Pharmacy dispense-scan initiated: pharmacyId={} fileName={} size={}",
                pharmacyId, file.getOriginalFilename(), file.getSize());

        PharmacyScanResponse response = scanHandler.handle(
                new PharmacyPrescriptionScanCommand(pharmacyId, file));

        MDC.remove("pharmacyId");
        return ResponseEntity.ok(response);
    }

    private void validateFileContentType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidPrescriptionException("File must not be empty");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidPrescriptionException(
                    "Invalid file type: '" + contentType
                            + "'. Accepted types: image/jpeg, image/png, application/pdf");
        }
    }
}
