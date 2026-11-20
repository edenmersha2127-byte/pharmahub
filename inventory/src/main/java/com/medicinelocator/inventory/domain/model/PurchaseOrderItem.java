package com.medicinelocator.inventory.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A single line on a purchase order: a quantity of one medicine at a unit cost,
 * destined to become a MedicineBatch when the order is received.
 */
public class PurchaseOrderItem {

    private UUID id;
    private UUID purchaseOrderId;
    private UUID medicineId;
    private int quantity;
    private BigDecimal unitCost;
    private String batchNumber;
    private LocalDate expiryDate;

    public PurchaseOrderItem() {
    }

    public PurchaseOrderItem(UUID id, UUID purchaseOrderId, UUID medicineId, int quantity,
                             BigDecimal unitCost, String batchNumber, LocalDate expiryDate) {
        this.id = id;
        this.purchaseOrderId = purchaseOrderId;
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
    }

    public BigDecimal lineCost() {
        if (unitCost == null) {
            return BigDecimal.ZERO;
        }
        return unitCost.multiply(BigDecimal.valueOf(quantity));
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(UUID purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }

    public UUID getMedicineId() { return medicineId; }
    public void setMedicineId(UUID medicineId) { this.medicineId = medicineId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
}
