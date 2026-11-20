package com.medicinelocator.inventory.application.command.handler;

import com.medicinelocator.inventory.application.command.DeductStockCommand;
import com.medicinelocator.inventory.application.dto.request.DeductBatchRequest;
import com.medicinelocator.inventory.domain.model.Medicine;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Deducts several medicines in a single transaction. Delegates each line to
 * {@link DeductStockHandler} (which shares this transaction via propagation),
 * so if any line fails — medicine missing or insufficient stock — the entire
 * cart deduction rolls back. Returns the resulting medicines for receipt build.
 */
@Component
public class BatchDeductHandler {

    private final DeductStockHandler deductStockHandler;

    public BatchDeductHandler(DeductStockHandler deductStockHandler) {
        this.deductStockHandler = deductStockHandler;
    }

    @Transactional
    public List<Medicine> handle(UUID pharmacyId, DeductBatchRequest request) {
        List<Medicine> results = new ArrayList<>();
        for (DeductBatchRequest.Item item : request.getItems()) {
            Medicine updated = deductStockHandler.handle(
                    new DeductStockCommand(pharmacyId, item.getMedicineId(), item.getQuantity()));
            results.add(updated);
        }
        return results;
    }
}
