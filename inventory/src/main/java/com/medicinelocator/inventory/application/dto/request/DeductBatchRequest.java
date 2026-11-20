package com.medicinelocator.inventory.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

/**
 * Atomic multi-line stock deduction, used by the sales-service at checkout so a
 * whole cart is deducted in one transaction (all-or-nothing).
 */
public class DeductBatchRequest {

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<Item> items;

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public static class Item {

        @NotNull(message = "medicineId is required")
        private UUID medicineId;

        @Positive(message = "Quantity must be positive")
        private int quantity;

        public UUID getMedicineId() { return medicineId; }
        public void setMedicineId(UUID medicineId) { this.medicineId = medicineId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
