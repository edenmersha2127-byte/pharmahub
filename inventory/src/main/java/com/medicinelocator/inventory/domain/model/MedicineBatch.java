package com.medicinelocator.inventory.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A received lot of a medicine. A medicine's denormalized stockQuantity equals
 * the sum of its batch quantities. Deductions consume batches FEFO (first
 * expiry, first out).
 */
public class MedicineBatch {

    private UUID id;
    private UUID medicineId;
    private UUID pharmacyId;
    private String batchNumber;
    private LocalDate expiryDate;
    private int quantity;
    private BigDecimal purchasePrice;
    private LocalDateTime receivedAt;

    public MedicineBatch() {
    }

    public MedicineBatch(UUID id, UUID medicineId, UUID pharmacyId, String batchNumber,
                         LocalDate expiryDate, int quantity, BigDecimal purchasePrice,
                         LocalDateTime receivedAt) {
        this.id = id;
        this.medicineId = medicineId;
        this.pharmacyId = pharmacyId;
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.receivedAt = receivedAt;
    }

    /**
     * Reduce this batch by up to {@code amount} units and return the number of
     * units actually taken (never more than the batch holds).
     */
    public int consume(int amount) {
        int taken = Math.min(this.quantity, Math.max(amount, 0));
        this.quantity -= taken;
        return taken;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getMedicineId() { return medicineId; }
    public void setMedicineId(UUID medicineId) { this.medicineId = medicineId; }

    public UUID getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
}
