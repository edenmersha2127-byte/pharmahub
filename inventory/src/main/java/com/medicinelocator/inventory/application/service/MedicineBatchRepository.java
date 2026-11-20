package com.medicinelocator.inventory.application.service;

import com.medicinelocator.inventory.domain.model.MedicineBatch;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Persistence port for medicine batches (lots).
 */
public interface MedicineBatchRepository {

    MedicineBatch save(MedicineBatch batch);

    /** All batches of a medicine ordered by expiry ascending (FEFO order). */
    List<MedicineBatch> findByMedicineIdFefo(UUID medicineId);

    /** Batches of a medicine with remaining quantity > 0, ordered FEFO. */
    List<MedicineBatch> findConsumableByMedicineIdFefo(UUID medicineId);

    List<MedicineBatch> findExpiringBetween(UUID pharmacyId, LocalDate from, LocalDate to);

    List<MedicineBatch> findExpired(UUID pharmacyId, LocalDate today);

    /** Expiring batches across ALL pharmacies (for the scheduled expiry scan). */
    List<MedicineBatch> findExpiringBetweenAllPharmacies(LocalDate from, LocalDate to);
}
