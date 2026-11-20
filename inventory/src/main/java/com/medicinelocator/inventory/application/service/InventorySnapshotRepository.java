package com.medicinelocator.inventory.application.service;

import com.medicinelocator.inventory.domain.model.InventorySnapshot;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for daily inventory snapshots (trend baselines).
 */
public interface InventorySnapshotRepository {

    InventorySnapshot save(InventorySnapshot snapshot);

    /** Whether a snapshot already exists for this pharmacy/date (scheduler idempotency). */
    boolean existsForDate(UUID pharmacyId, LocalDate snapshotDate);

    /** The most recent snapshot on or before {@code date}, used as the trend baseline. */
    Optional<InventorySnapshot> findLatestOnOrBefore(UUID pharmacyId, LocalDate date);
}
