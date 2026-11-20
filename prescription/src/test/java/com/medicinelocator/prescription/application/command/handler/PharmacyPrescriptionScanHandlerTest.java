package com.medicinelocator.prescription.application.command.handler;

import com.medicinelocator.prescription.application.command.PharmacyPrescriptionScanCommand;
import com.medicinelocator.prescription.application.dto.response.PharmacyScanResponse;
import com.medicinelocator.prescription.application.service.InventoryLookupClient;
import com.medicinelocator.prescription.application.service.MedicineExtractorService;
import com.medicinelocator.prescription.application.service.OcrService;
import com.medicinelocator.prescription.application.verification.PrescriptionVerificationService;
import com.medicinelocator.prescription.domain.model.VerificationOutcome;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PharmacyPrescriptionScanHandler")
class PharmacyPrescriptionScanHandlerTest {

    @Mock private PrescriptionVerificationService verificationService;
    @Mock private OcrService ocrService;
    @Mock private MedicineExtractorService medicineExtractorService;
    @Mock private InventoryLookupClient inventoryLookupClient;
    @Mock private MultipartFile file;

    private PharmacyPrescriptionScanHandler handler;

    private final UUID pharmacyId = UUID.randomUUID();

    private PharmacyPrescriptionScanHandler handler() {
        if (handler == null) {
            handler = new PharmacyPrescriptionScanHandler(
                    verificationService, ocrService, medicineExtractorService, inventoryLookupClient);
        }
        return handler;
    }

    @Test
    @DisplayName("INVALID prescription is hard-blocked and skips extraction")
    void invalid_isBlocked() {
        when(verificationService.verify(any())).thenReturn(VerificationOutcome.invalid("H1", "RX1"));

        PharmacyScanResponse response = handler().handle(
                new PharmacyPrescriptionScanCommand(pharmacyId, file));

        assertEquals("INVALID", response.verificationStatus());
        assertFalse(response.verified());
        assertFalse(response.canDispense());
        assertTrue(response.extractedMedicines().isEmpty());
        assertTrue(response.medicines().isEmpty());
        verify(ocrService, never()).extractText(any());
        verify(medicineExtractorService, never()).extract(any());
        verify(inventoryLookupClient, never()).lookup(any(), any());
    }

    @Test
    @DisplayName("VALID prescription extracts and resolves medicines against the pharmacy catalogue")
    void valid_resolvesMedicines() {
        UUID medicineId = UUID.randomUUID();
        when(verificationService.verify(any())).thenReturn(VerificationOutcome.valid("H1", "RX1"));
        when(ocrService.extractText(any())).thenReturn("raw ocr");
        when(medicineExtractorService.extract("raw ocr")).thenReturn(List.of("Amoxicillin"));
        when(inventoryLookupClient.lookup(eq(pharmacyId), eq("Amoxicillin")))
                .thenReturn(List.of(new InventoryLookupClient.Medicine(
                        medicineId, "Amoxicillin", new BigDecimal("12.50"), 100, true)));

        PharmacyScanResponse response = handler().handle(
                new PharmacyPrescriptionScanCommand(pharmacyId, file));

        assertEquals("VALID", response.verificationStatus());
        assertTrue(response.verified());
        assertTrue(response.canDispense());
        assertEquals(List.of("Amoxicillin"), response.extractedMedicines());
        assertEquals(1, response.medicines().size());
        var med = response.medicines().get(0);
        assertTrue(med.matched());
        assertEquals(medicineId, med.medicineId());
        assertEquals(new BigDecimal("12.50"), med.price());
        assertEquals(100, med.stockQuantity());
        assertTrue(med.inStock());
    }

    @Test
    @DisplayName("UNVERIFIABLE (amber) still allows dispensing; unmatched names come back unmatched")
    void amber_allowsDispenseWithUnmatched() {
        when(verificationService.verify(any()))
                .thenReturn(VerificationOutcome.unverifiable("H1", "RX1", "hospital timeout"));
        when(ocrService.extractText(any())).thenReturn("raw ocr");
        when(medicineExtractorService.extract("raw ocr")).thenReturn(List.of("Paracetamol"));
        when(inventoryLookupClient.lookup(eq(pharmacyId), eq("Paracetamol"))).thenReturn(List.of());

        PharmacyScanResponse response = handler().handle(
                new PharmacyPrescriptionScanCommand(pharmacyId, file));

        assertEquals("UNVERIFIABLE", response.verificationStatus());
        assertFalse(response.verified());
        assertTrue(response.canDispense());
        assertEquals(1, response.medicines().size());
        var med = response.medicines().get(0);
        assertFalse(med.matched());
        assertNull(med.medicineId());
        assertFalse(med.inStock());
    }
}
