package com.medicinelocator.inventory.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A daily point-in-time snapshot of a pharmacy's catalogue size and inventory
 * value. Written once per day by {@code InventorySnapshotScheduler} and read
 * back to compute month-over-month trend deltas for the dashboard. Snapshots
 * accrue going forward — there is no historical backfill.
 */
public class InventorySnapshot {

    private UUID id;
    private UUID pharmacyId;
    private LocalDate snapshotDate;
    private long medicineCount;
    private BigDecimal inventoryValue;
    private LocalDateTime createdAt;

    public InventorySnapshot() {
    }

    public InventorySnapshot(UUID id, UUID pharmacyId, LocalDate snapshotDate,
                             long medicineCount, BigDecimal inventoryValue, LocalDateTime createdAt) {
        this.id = id;
        this.pharmacyId = pharmacyId;
        this.snapshotDate = snapshotDate;
        this.medicineCount = medicineCount;
        this.inventoryValue = inventoryValue;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }

    public LocalDate getSnapshotDate() { return snapshotDate; }
    public void setSnapshotDate(LocalDate snapshotDate) { this.snapshotDate = snapshotDate; }

    public long getMedicineCount() { return medicineCount; }
    public void setMedicineCount(long medicineCount) { this.medicineCount = medicineCount; }

    public BigDecimal getInventoryValue() { return inventoryValue; }
    public void setInventoryValue(BigDecimal inventoryValue) { this.inventoryValue = inventoryValue; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
