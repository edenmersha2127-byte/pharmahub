package com.medicinelocator.auth.application.command.handler;

import com.medicinelocator.auth.application.command.ApprovePharmacyCommand;
import com.medicinelocator.auth.application.service.PharmacyService;
import com.medicinelocator.auth.domain.enums.AccountStatus;
import com.medicinelocator.auth.domain.enums.PharmacyStatus;
import com.medicinelocator.auth.domain.exception.PharmacyNotFoundException;
import com.medicinelocator.auth.domain.model.Pharmacy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovePharmacyHandler")
class ApprovePharmacyHandlerTest {

    @Mock private PharmacyService pharmacyService;

    @InjectMocks
    private ApprovePharmacyHandler handler;

    private Pharmacy pendingPharmacy(UUID id) {
        return new Pharmacy(
                id, "pharm@example.com", "hash", "ABC Pharmacy",
                "LIC-1", "0911", "1 St", "Addis", 9.0, 38.7,
                AccountStatus.PENDING, PharmacyStatus.PENDING, true, 0, null,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("approves a pending pharmacy and returns the updated entity")
    void approve_success() {
        UUID id = UUID.randomUUID();
        Pharmacy pharmacy = pendingPharmacy(id);

        when(pharmacyService.findById(id)).thenReturn(Optional.of(pharmacy));
        when(pharmacyService.update(any(Pharmacy.class))).thenAnswer(inv -> inv.getArgument(0));

        Pharmacy result = handler.handle(new ApprovePharmacyCommand(id));

        assertEquals(PharmacyStatus.APPROVED, result.getPharmacyStatus());
        assertEquals(AccountStatus.ACTIVE, result.getAccountStatus());
        verify(pharmacyService).update(pharmacy);
    }

    @Test
    @DisplayName("throws PharmacyNotFoundException when pharmacy id does not exist")
    void approve_throwsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(pharmacyService.findById(id)).thenReturn(Optional.empty());

        assertThrows(PharmacyNotFoundException.class,
                () -> handler.handle(new ApprovePharmacyCommand(id)));

        verify(pharmacyService, never()).update(any());
    }
}
