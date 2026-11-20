package com.medicinelocator.inventory.application.service;

import com.medicinelocator.inventory.domain.model.MedicineBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Coordinates batch-level stock changes while keeping the medicine's
 * denormalized stockQuantity consistent (total = sum of batch quantities).
 */
@Component
public class BatchStockService {

    private static final Logger log = LoggerFactory.getLogger(BatchStockService.class);

    private final MedicineBatchRepository batchRepository;

    public BatchStockService(MedicineBatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    /**
     * Consume {@code amount} units from a medicine's batches in FEFO order
     * (earliest expiry first). Returns the number of units actually consumed
     * from batches. If the medicine has no batches (legacy data), returns 0 so
     * the caller can fall back to decrementing the medicine directly.
     */
    public int consumeFefo(UUID medicineId, int amount) {
        List<MedicineBatch> batches = batchRepository.findConsumableByMedicineIdFefo(medicineId);
        if (batches.isEmpty()) {
            return 0;
        }

        int remaining = amount;
        int consumed = 0;
        for (MedicineBatch batch : batches) {
            if (remaining <= 0) {
                break;
            }
            int taken = batch.consume(remaining);
            if (taken > 0) {
                remaining -= taken;
                consumed += taken;
                batchRepository.save(batch);
            }
        }

        log.debug("FEFO consumption: medicineId={} requested={} consumedFromBatches={}",
                medicineId, amount, consumed);
        return consumed;
    }
}
