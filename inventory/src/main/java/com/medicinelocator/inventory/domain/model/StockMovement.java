package com.medicinelocator.inventory.domain.model;

import com.medicinelocator.inventory.domain.enums.StockMovementType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable audit record of a single change to a medicine's stock quantity.
 */
public class StockMovement {

    private UUID id;
    private UUID medicineId;
    private UUID pharmacyId;
    private StockMovementType type;
    private int quantityChange;
    private int resultingQuantity;
    private String note;
    private UUID createdBy;
    private LocalDateTime createdAt;

    public StockMovement() {
    }

    public StockMovement(UUID id, UUID medicineId, UUID pharmacyId, StockMovementType type,
                         int quantityChange, int resultingQuantity, String note,
                         UUID createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.medicineId = medicineId;
        this.pharmacyId = pharmacyId;
        this.type = type;
        this.quantityChange = quantityChange;
        this.resultingQuantity = resultingQuantity;
        this.note = note;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getMedicineId() { return medicineId; }
    public void setMedicineId(UUID medicineId) { this.medicineId = medicineId; }

    public UUID getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }

    public StockMovementType getType() { return type; }
    public void setType(StockMovementType type) { this.type = type; }

    public int getQuantityChange() { return quantityChange; }
    public void setQuantityChange(int quantityChange) { this.quantityChange = quantityChange; }

    public int getResultingQuantity() { return resultingQuantity; }
    public void setResultingQuantity(int resultingQuantity) { this.resultingQuantity = resultingQuantity; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
