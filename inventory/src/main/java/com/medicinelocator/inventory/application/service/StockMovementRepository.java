package com.medicinelocator.inventory.application.service;

import com.medicinelocator.inventory.domain.model.StockMovement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Persistence port for the stock-movement audit log.
 */
public interface StockMovementRepository {

    StockMovement save(StockMovement movement);

    List<StockMovement> findByMedicineIdAndPharmacyId(UUID medicineId, UUID pharmacyId);

    /** All movements for a pharmacy within a time window, oldest first. */
    List<StockMovement> findByPharmacyIdBetween(UUID pharmacyId, LocalDateTime from, LocalDateTime to);
}
