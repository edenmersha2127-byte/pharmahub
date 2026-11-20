package com.medicinelocator.salesservice.domain.model;

import com.medicinelocator.salesservice.domain.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A completed POS sale: the source of truth for what was sold, when, by whom.
 * Stock is deducted in inventory as part of checkout; this aggregate records the
 * financial/audit side.
 */
public class Sale {

    private UUID id;
    private UUID pharmacyId;
    private UUID customerId;      // nullable — walk-in without a CRM record
    private UUID cashierId;       // staff/owner who rang the sale
    private PaymentMethod paymentMethod;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private List<SaleItem> items = new ArrayList<>();

    public Sale() {
    }

    public Sale(UUID id, UUID pharmacyId, UUID customerId, UUID cashierId,
                PaymentMethod paymentMethod, BigDecimal total, LocalDateTime createdAt) {
        this.id = id;
        this.pharmacyId = pharmacyId;
        this.customerId = customerId;
        this.cashierId = cashierId;
        this.paymentMethod = paymentMethod;
        this.total = total;
        this.createdAt = createdAt;
    }

    public boolean belongsTo(UUID pharmacyId) {
        return this.pharmacyId.equals(pharmacyId);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPharmacyId() { return pharmacyId; }
    public void setPharmacyId(UUID pharmacyId) { this.pharmacyId = pharmacyId; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getCashierId() { return cashierId; }
    public void setCashierId(UUID cashierId) { this.cashierId = cashierId; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}
