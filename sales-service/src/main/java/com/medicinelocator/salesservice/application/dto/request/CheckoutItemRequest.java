package com.medicinelocator.salesservice.application.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public class CheckoutItemRequest {

    @NotNull(message = "medicineId is required")
    private UUID medicineId;

    @Positive(message = "Quantity must be positive")
    private int quantity;

    public UUID getMedicineId() { return medicineId; }
    public void setMedicineId(UUID medicineId) { this.medicineId = medicineId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
