package com.medicinelocator.inventory.domain.model;

import com.medicinelocator.inventory.domain.enums.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A purchase order (goods-received note) raised against a supplier. Starts as
 * DRAFT and becomes RECEIVED when its items are booked into stock as batches.
 */
public class PurchaseOrder {

    private UUID id;
    private UUID pharmacyId;
    private UUID supplierId;
    private PurchaseOrderStatus status;
    private BigDecimal totalCost;
    private LocalDateTime createdAt;
    private LocalDateTime receivedAt;
    private List<PurchaseOrderItem> items = new ArrayList<>();

    public PurchaseOrder() {
    }

    public PurchaseOrder(UUID id, UUID pharmacyId, UUID supplierId, PurchaseOrderStatus status,
                         BigDecimal totalCost, LocalDateTime createdAt, LocalDateTime receivedAt) {
        this.id = id;
        this.pharmacyId = pharmacyId;
        this.supplierId = supplierId;
        this.status = status;
        this.totalCost = totalCost;
        this.createdAt = createdAt;
        this.receivedAt = receivedAt;
    }

    public boolean isReceived() {
        return status == PurchaseOrderStatus.RECEIVED;
    }

    public void markReceived(LocalDateTime when) {
        this.status = PurchaseOrderStatus.RECEIVED;
        this.receivedAt = when;
    }

    public boolean belongsTo(UUID pharmacyId) {
        return this.pharmacyId.equals(pharmacyId);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }

    public UUID getSupplierId() { return supplierId; }
    public void setSupplierId(UUID supplierId) { this.supplierId = supplierId; }

    public PurchaseOrderStatus getStatus() { return status; }
    public void setStatus(PurchaseOrderStatus status) { this.status = status; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    public List<PurchaseOrderItem> getItems() { return items; }
    public void setItems(List<PurchaseOrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}
