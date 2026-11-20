package com.medicinelocator.salesservice.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A single line of a completed sale. Price and name are snapshotted at sale time
 * so the receipt/record stays accurate even if the medicine changes later.
 */
public class SaleItem {

    private UUID id;
    private UUID saleId;
    private UUID medicineId;
    private String medicineName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal purchasePrice; // cost snapshot at sale time, for profit reporting

    public SaleItem() {
    }

    public SaleItem(UUID id, UUID saleId, UUID medicineId, String medicineName,
                    int quantity, BigDecimal unitPrice, BigDecimal purchasePrice) {
        this.id = id;
        this.saleId = saleId;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal lineTotal() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /** Profit for this line = (sell - buy) * quantity. Treats null cost as zero. */
    public BigDecimal lineProfit() {
        BigDecimal sell = unitPrice != null ? unitPrice : BigDecimal.ZERO;
        BigDecimal buy = purchasePrice != null ? purchasePrice : BigDecimal.ZERO;
        return sell.subtract(buy).multiply(BigDecimal.valueOf(quantity));
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSaleId() { return saleId; }
    public void setSaleId(UUID saleId) { this.saleId = saleId; }

    public UUID getMedicineId() { return medicineId; }
    public void setMedicineId(UUID medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
}
