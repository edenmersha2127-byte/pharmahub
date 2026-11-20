package com.medicinelocator.search.application.command.handler;

import com.medicinelocator.search.application.command.IndexMedicineCommand;
import com.medicinelocator.search.application.service.UpsertSearchIndexService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("IndexMedicineHandler")
class IndexMedicineHandlerTest {

    @Mock private UpsertSearchIndexService upsertSearchIndexService;

    @InjectMocks
    private IndexMedicineHandler handler;

    private IndexMedicineCommand sampleCommand() {
        return new IndexMedicineCommand(
                UUID.randomUUID(), "Paracetamol", "Acetaminophen", "Panadol",
                "Analgesic", "Pain reliever", false,
                new BigDecimal("12.50"), 50, true, true,
                UUID.randomUUID(), "ABC Pharmacy", "1 Main St", "Addis Ababa",
                9.0, 38.7, true
        );
    }

    @Test
    @DisplayName("delegates exactly one upsert call to the service")
    void handle_delegatesToService() {
        IndexMedicineCommand cmd = sampleCommand();
        handler.handle(cmd);
        verify(upsertSearchIndexService, times(1)).upsert(cmd);
    }
}
