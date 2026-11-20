package com.medicinelocator.inventory.application.service;

import com.medicinelocator.inventory.domain.enums.StockMovementType;
import com.medicinelocator.inventory.domain.model.Medicine;
import com.medicinelocator.inventory.domain.model.StockMovement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Writes entries to the stock-movement audit log. Recording is best-effort:
 * an audit-log failure must never break the inventory operation that triggered it.
 */
@Component
public class StockMovementRecorder {

    private static final Logger log = LoggerFactory.getLogger(StockMovementRecorder.class);

    private final StockMovementRepository stockMovementRepository;

    public StockMovementRecorder(StockMovementRepository stockMovementRepository) {
        this.stockMovementRepository = stockMovementRepository;
    }

    public void record(Medicine medicine, StockMovementType type, int quantityChange,
                       int resultingQuantity, String note, UUID createdBy) {
        try {
            StockMovement movement = new StockMovement(
                    UUID.randomUUID(),
                    medicine.getId(),
                    medicine.getPharmacyId(),
                    type,
                    quantityChange,
                    resultingQuantity,
                    note,
                    createdBy,
                    LocalDateTime.now()
            );
            stockMovementRepository.save(movement);
        } catch (Exception e) {
            log.error("Failed to record stock movement: medicineId={} type={} error={}",
                    medicine.getId(), type, e.getMessage(), e);
        }
    }
}
