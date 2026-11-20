package com.medicinelocator.prescription.application.command.handler;

import com.medicinelocator.prescription.application.command.PharmacyPrescriptionScanCommand;
import com.medicinelocator.prescription.application.dto.response.PharmacyScanResponse;
import com.medicinelocator.prescription.application.dto.response.ResolvedMedicineResponse;
import com.medicinelocator.prescription.application.service.InventoryLookupClient;
import com.medicinelocator.prescription.application.service.MedicineExtractorService;
import com.medicinelocator.prescription.application.service.OcrService;
import com.medicinelocator.prescription.application.verification.PrescriptionVerificationService;
import com.medicinelocator.prescription.domain.enums.VerificationStatus;
import com.medicinelocator.prescription.domain.model.VerificationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Pharmacy dispense-scan flow: verify the prescription against its issuing
 * hospital, then (unless it is confirmed INVALID) OCR-extract the prescribed
 * medicines and resolve them against the pharmacy's own catalogue so they can be
 * loaded into the POS. Amber outcomes (not integrated / unreadable / hospital
 * unreachable) still allow dispensing on the pharmacist's judgment; only a
 * confirmed INVALID blocks it.
 */
@Component
public class PharmacyPrescriptionScanHandler {

    private static final Logger log = LoggerFactory.getLogger(PharmacyPrescriptionScanHandler.class);

    private final PrescriptionVerificationService verificationService;
    private final OcrService ocrService;
    private final MedicineExtractorService medicineExtractorService;
    private final InventoryLookupClient inventoryLookupClient;

    public PharmacyPrescriptionScanHandler(PrescriptionVerificationService verificationService,
                                           OcrService ocrService,
                                           MedicineExtractorService medicineExtractorService,
                                           InventoryLookupClient inventoryLookupClient) {
        this.verificationService = verificationService;
        this.ocrService = ocrService;
        this.medicineExtractorService = medicineExtractorService;
        this.inventoryLookupClient = inventoryLookupClient;
    }

    public PharmacyScanResponse handle(PharmacyPrescriptionScanCommand command) {
        VerificationOutcome outcome = verificationService.verify(command.getFile());
        String status = outcome.getStatus().name();

        // Confirmed invalid by the issuing hospital: hard-block, skip extraction.
        if (outcome.getStatus() == VerificationStatus.INVALID) {
            log.info("Dispense-scan blocked: pharmacyId={} status=INVALID", command.getPharmacyId());
            return new PharmacyScanResponse(status, false, false, outcome.getMessage(),
                    List.of(), List.of(),
                    "Prescription is invalid — the issuing hospital has no record of it. Do not dispense.");
        }

        boolean verified = outcome.isValid();

        // Extract prescribed medicine names and resolve them against this pharmacy's catalogue.
        String rawExtractionOutput = ocrService.extractText(command.getFile());
        List<String> extractedNames = medicineExtractorService.extract(rawExtractionOutput);

        List<ResolvedMedicineResponse> resolved = new ArrayList<>(extractedNames.size());
        for (String name : extractedNames) {
            List<InventoryLookupClient.Medicine> matches =
                    inventoryLookupClient.lookup(command.getPharmacyId(), name);
            if (matches.isEmpty()) {
                resolved.add(new ResolvedMedicineResponse(name, false, null, null, null, null, false));
            } else {
                InventoryLookupClient.Medicine best = matches.get(0);
                resolved.add(new ResolvedMedicineResponse(
                        name, true, best.medicineId(), best.name(), best.price(),
                        best.stockQuantity(), best.stockQuantity() > 0));
            }
        }

        String message = verified
                ? "Prescription verified with the issuing hospital."
                : "Could not verify with an issuing hospital (" + outcome.getMessage()
                        + "). Proceed on your professional judgment.";

        log.info("Dispense-scan complete: pharmacyId={} status={} extracted={} matched={}",
                command.getPharmacyId(), status, extractedNames.size(),
                resolved.stream().filter(ResolvedMedicineResponse::matched).count());

        return new PharmacyScanResponse(status, verified, true, outcome.getMessage(),
                extractedNames, resolved, message);
    }
}
