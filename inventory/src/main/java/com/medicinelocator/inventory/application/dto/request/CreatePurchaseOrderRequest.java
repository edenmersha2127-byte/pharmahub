package com.medicinelocator.inventory.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public class CreatePurchaseOrderRequest {

    private UUID supplierId;

    @NotEmpty(message = "A purchase order must contain at least one item")
    @Valid
    private List<PurchaseOrderItemRequest> items;

    public UUID getSupplierId() { return supplierId; }
    public void setSupplierId(UUID supplierId) { this.supplierId = supplierId; }

    public List<PurchaseOrderItemRequest> getItems() { return items; }
    public void setItems(List<PurchaseOrderItemRequest> items) { this.items = items; }
}
