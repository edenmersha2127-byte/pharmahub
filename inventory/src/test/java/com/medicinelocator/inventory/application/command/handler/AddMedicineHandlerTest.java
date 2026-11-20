package com.medicinelocator.inventory.application.command.handler;

import com.medicinelocator.inventory.application.command.AddMedicineCommand;
import com.medicinelocator.inventory.application.dto.response.PharmacyProfileResponse;
import com.medicinelocator.inventory.application.service.MedicineService;
import com.medicinelocator.inventory.application.service.PharmacyProfileService;
import com.medicinelocator.inventory.application.service.SearchIndexClient;
import com.medicinelocator.inventory.application.service.StockMovementRecorder;
import com.medicinelocator.inventory.application.service.StockNotificationPublisher;
import com.medicinelocator.inventory.domain.exception.DuplicateMedicineException;
import com.medicinelocator.inventory.domain.model.Medicine;
import com.medicinelocator.inventory.infrastructure.redis.InventoryCacheService;
import com.medicinelocator.inventory.infrastructure.security.CurrentUserProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddMedicineHandler")
class AddMedicineHandlerTest {

    @Mock private MedicineService medicineService;
    @Mock private PharmacyProfileService pharmacyProfileService;
    @Mock private SearchIndexClient searchIndexClient;
    @Mock private InventoryCacheService cacheService;
    @Mock private StockNotificationPublisher stockNotificationPublisher;
    @Mock private StockMovementRecorder stockMovementRecorder;
    @Mock private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private AddMedicineHandler handler;

    private AddMedicineCommand validCommand(UUID pharmacyId) {
        return new AddMedicineCommand(
                pharmacyId,
                "Paracetamol",
                "Acetaminophen",
                "Panadol",
                "Analgesic",
                "Pain reliever",
                new BigDecimal("12.50"),
                100,
                false,
                LocalDate.now().plusYears(2),
                new BigDecimal("8.00"),
                10,
                null,
                null,
                null
        );
    }

    @Test
    @DisplayName("saves a medicine and pushes to search index on success")
    void add_success() {
        UUID pharmacyId = UUID.randomUUID();
        AddMedicineCommand cmd = validCommand(pharmacyId);

        when(medicineService.existsByPharmacyIdAndMedicineName(pharmacyId, "Paracetamol"))
                .thenReturn(false);
        when(medicineService.save(any(Medicine.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        PharmacyProfileResponse profile = new PharmacyProfileResponse();
        profile.setPharmacyName("ABC Pharmacy");
        profile.setAddress("1 Main St");
        profile.setCity("Addis Ababa");
        profile.setLatitude(9.0);
        profile.setLongitude(38.7);
        when(pharmacyProfileService.findById(pharmacyId)).thenReturn(Optional.of(profile));

        Medicine result = handler.handle(cmd);

        assertNotNull(result);
        assertEquals("Paracetamol", result.getMedicineName());
        assertEquals(100, result.getStockQuantity());
        assertTrue(result.isAvailable());
        verify(cacheService).evictPharmacyInventoryCache(pharmacyId);
        verify(searchIndexClient).indexMedicine(any(Medicine.class),
                eq("ABC Pharmacy"), eq("1 Main St"), eq("Addis Ababa"), eq(9.0), eq(38.7), anyBoolean());
    }

    @Test
    @DisplayName("throws DuplicateMedicineException when medicine already exists for this pharmacy")
    void add_throwsOnDuplicate() {
        UUID pharmacyId = UUID.randomUUID();
        AddMedicineCommand cmd = validCommand(pharmacyId);
        when(medicineService.existsByPharmacyIdAndMedicineName(pharmacyId, "Paracetamol"))
                .thenReturn(true);

        assertThrows(DuplicateMedicineException.class, () -> handler.handle(cmd));
        verify(medicineService, never()).save(any());
    }

    @Test
    @DisplayName("medicine with zero stock is saved as unavailable")
    void add_zeroStockMarksUnavailable() {
        UUID pharmacyId = UUID.randomUUID();
        AddMedicineCommand cmd = new AddMedicineCommand(
                pharmacyId, "ZeroStock", null, null, null, null,
                BigDecimal.ONE, 0, false, LocalDate.now().plusMonths(6),
                null, 0, null, null, null);

        when(medicineService.existsByPharmacyIdAndMedicineName(pharmacyId, "ZeroStock"))
                .thenReturn(false);
        when(medicineService.save(any(Medicine.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(pharmacyProfileService.findById(pharmacyId))
                .thenReturn(Optional.of(new PharmacyProfileResponse()));

        Medicine result = handler.handle(cmd);

        assertFalse(result.isAvailable());
    }

    @Test
    @DisplayName("search index push failure does NOT fail the whole operation (logged only)")
    void add_searchIndexFailureDoesNotPropagate() {
        UUID pharmacyId = UUID.randomUUID();
        AddMedicineCommand cmd = validCommand(pharmacyId);

        when(medicineService.existsByPharmacyIdAndMedicineName(pharmacyId, "Paracetamol"))
                .thenReturn(false);
        when(medicineService.save(any(Medicine.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(pharmacyProfileService.findById(pharmacyId))
                .thenReturn(Optional.empty()); // forces RuntimeException inside pushToSearchIndex

        Medicine result = handler.handle(cmd); // should not throw

        assertNotNull(result);
        verify(searchIndexClient, never()).indexMedicine(any(), any(), any(), any(), anyDouble(), anyDouble(), anyBoolean());
    }
}
