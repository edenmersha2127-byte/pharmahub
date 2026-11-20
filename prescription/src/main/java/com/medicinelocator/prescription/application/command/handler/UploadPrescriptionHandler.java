package com.medicinelocator.prescription.application.command.handler;

import com.medicinelocator.prescription.application.command.UploadPrescriptionCommand;
import com.medicinelocator.prescription.application.dto.response.ExtractedMedicinesResponse;
import com.medicinelocator.prescription.application.service.ImageStoragePort;
import com.medicinelocator.prescription.application.service.MedicineExtractorService;
import com.medicinelocator.prescription.application.service.NotificationClient;
import com.medicinelocator.prescription.application.service.OcrService;
import com.medicinelocator.prescription.application.service.PrescriptionService;
import com.medicinelocator.prescription.application.service.SearchServiceClient;
import com.medicinelocator.prescription.application.verification.PrescriptionVerificationService;
import com.medicinelocator.prescription.domain.enums.PrescriptionStatus;
import com.medicinelocator.prescription.domain.exception.InvalidPrescriptionException;
import com.medicinelocator.prescription.domain.exception.OcrProcessingException;
import com.medicinelocator.prescription.domain.model.ExtractedMedicine;
import com.medicinelocator.prescription.domain.model.Prescription;
import com.medicinelocator.prescription.domain.model.VerificationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class UploadPrescriptionHandler {

    private static final Logger log = LoggerFactory.getLogger(UploadPrescriptionHandler.class);

    private final PrescriptionService prescriptionService;
    private final OcrService ocrService;
    private final MedicineExtractorService medicineExtractorService;
    private final ImageStoragePort imageStoragePort;
    private final SearchServiceClient searchServiceClient;
    private final PrescriptionVerificationService prescriptionVerificationService;
    private final NotificationClient notificationClient;

    public UploadPrescriptionHandler(PrescriptionService prescriptionService,
                                     OcrService ocrService,
                                     MedicineExtractorService medicineExtractorService,
                                     ImageStoragePort imageStoragePort,
                                     SearchServiceClient searchServiceClient,
                                     PrescriptionVerificationService prescriptionVerificationService,
                                     NotificationClient notificationClient) {
        this.prescriptionService = prescriptionService;
        this.ocrService = ocrService;
        this.medicineExtractorService = medicineExtractorService;
        this.imageStoragePort = imageStoragePort;
        this.searchServiceClient = searchServiceClient;
        this.prescriptionVerificationService = prescriptionVerificationService;
        this.notificationClient = notificationClient;
    }

    @Transactional
    public ExtractedMedicinesResponse handle(UploadPrescriptionCommand command) {
        validateFile(command);

        UUID prescriptionId = UUID.randomUUID();
        MDC.put("prescriptionId", prescriptionId.toString());
        MDC.put("customerId", command.getCustomerId().toString());

        log.info("Starting prescription upload: prescriptionId={} customerId={}",
                prescriptionId, command.getCustomerId());

        // STEP 1 — Store file
        String imageUrl = imageStoragePort.store(
                command.getFile(),
                command.getCustomerId().toString()
        );
        log.debug("File stored: imageUrl={}", imageUrl);

        // STEP 2 — Persist prescription record in PENDING state
        Prescription prescription = new Prescription(
                prescriptionId,
                command.getCustomerId(),
                imageUrl,
                null,
                PrescriptionStatus.PENDING,
                command.getLatitude(),
                command.getLongitude(),
                List.of(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        prescription = prescriptionService.save(prescription);

        // STEP 2.5 — VERIFICATION GATE: confirm this prescription exists at its
        // issuing hospital before doing any medicine extraction or pharmacy search.
        // Uses a SEPARATE identity-extraction pass; the medicine flow below is untouched.
        VerificationOutcome verification = prescriptionVerificationService.verify(command.getFile());
        if (!verification.isValid()) {
            log.info("Verification gate stopped processing: prescriptionId={} status={} message={}",
                    prescriptionId, verification.getStatus(), verification.getMessage());
            prescription.markFailed();
            prescriptionService.update(prescription);

            notificationClient.notify(
                    command.getCustomerId(),
                    "CUSTOMER",
                    "PRESCRIPTION_NEEDS_REVIEW",
                    "Prescription needs review",
                    "We couldn't verify your prescription: " + verification.getMessage()
                            + " Please re-upload a clear image.",
                    "/prescriptions");

            MDC.remove("prescriptionId");
            MDC.remove("customerId");

            ExtractedMedicinesResponse rejected = new ExtractedMedicinesResponse();
            rejected.setPrescriptionId(prescriptionId);
            rejected.setExtractedMedicines(List.of());
            rejected.setPharmacyResults(null);
            rejected.setVerified(false);
            rejected.setVerificationMessage(verification.getMessage());
            rejected.setMessage(verification.getMessage());
            return rejected;
        }
        log.info("Verification gate passed: prescriptionId={} hospitalId={} issuingRxId={}",
                prescriptionId, verification.getHospitalId(), verification.getPrescriptionId());

        // STEP 3 — OCR / Vision extraction (Gemini Vision by default)
        String rawExtractionOutput;
        try {
            prescription.markProcessing();
            prescriptionService.update(prescription);
            rawExtractionOutput = ocrService.extractText(command.getFile());
            log.debug("Vision extraction completed: prescriptionId={} outputLength={}",
                    prescriptionId, rawExtractionOutput != null ? rawExtractionOutput.length() : 0);
        } catch (OcrProcessingException e) {
            log.error("OCR/Vision extraction failed: prescriptionId={} error={}",
                    prescriptionId, e.getMessage(), e);
            prescription.markFailed();
            prescriptionService.update(prescription);
            notifyNeedsReview(command.getCustomerId());
            MDC.remove("prescriptionId");
            MDC.remove("customerId");
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during extraction: prescriptionId={} error={}",
                    prescriptionId, e.getMessage(), e);
            prescription.markFailed();
            prescriptionService.update(prescription);
            notifyNeedsReview(command.getCustomerId());
            MDC.remove("prescriptionId");
            MDC.remove("customerId");
            throw new OcrProcessingException(
                    "Unexpected error during prescription extraction: " + e.getMessage(), e);
        }

        // STEP 4 — Extract + normalise medicine names
        List<String> normalizedNames = medicineExtractorService.extract(rawExtractionOutput);

        if (normalizedNames.isEmpty()) {
            log.warn("No medicines extracted from prescription: prescriptionId={} rawOutput={}",
                    prescriptionId, rawExtractionOutput);
        } else {
            log.info("Medicines extracted: prescriptionId={} count={} medicines={}",
                    prescriptionId, normalizedNames.size(), normalizedNames);
        }

        List<ExtractedMedicine> medicines = normalizedNames.stream()
                .map(name -> ExtractedMedicine.of(prescriptionId, name))
                .toList();

        prescription.completeProcessing(rawExtractionOutput, medicines);
        prescriptionService.update(prescription);

        if (normalizedNames.isEmpty()) {
            notifyNeedsReview(command.getCustomerId());
        } else {
            notificationClient.notify(
                    command.getCustomerId(),
                    "CUSTOMER",
                    "PRESCRIPTION_PROCESSED",
                    "Prescription processed",
                    "Your prescription was processed and " + normalizedNames.size()
                            + " medicine(s) were found. View matching pharmacies.",
                    "/prescriptions");
        }

        // STEP 5 — Forward to Search Service (fuzzy matching happens server-side
        // in Search Service's pg_trgm similarity search, so we send the raw
        // extracted names as-is; Search Service handles "Paracetmol" → "Paracetamol")
        Object pharmacyResults = null;
        if (!normalizedNames.isEmpty()) {
            try {
                pharmacyResults = searchServiceClient.searchMedicines(
                        normalizedNames,
                        command.getLatitude(),
                        command.getLongitude(),
                        command.getRadiusKm()
                );
                log.info("Search service call successful: prescriptionId={}", prescriptionId);
            } catch (Exception e) {
                log.warn("Search service call failed (non-fatal): prescriptionId={} error={}",
                        prescriptionId, e.getMessage());
            }
        }

        // STEP 6 — Build response
        ExtractedMedicinesResponse response = new ExtractedMedicinesResponse();
        response.setPrescriptionId(prescriptionId);
        response.setExtractedMedicines(normalizedNames);
        response.setPharmacyResults(pharmacyResults);
        response.setVerified(true);
        response.setVerificationMessage(verification.getMessage());
        response.setMessage(
                normalizedNames.isEmpty()
                        ? "No medicine names could be extracted from the prescription image"
                        : "Prescription processed successfully"
        );

        MDC.remove("prescriptionId");
        MDC.remove("customerId");

        return response;
    }

    private void validateFile(UploadPrescriptionCommand command) {
        if (command.getFile() == null || command.getFile().isEmpty()) {
            throw new InvalidPrescriptionException("Prescription file is required and must not be empty");
        }
    }

    private void notifyNeedsReview(UUID customerId) {
        notificationClient.notify(
                customerId,
                "CUSTOMER",
                "PRESCRIPTION_NEEDS_REVIEW",
                "Prescription needs review",
                "We couldn't read your prescription image. Please upload a clearer photo.",
                "/prescriptions");
    }
}