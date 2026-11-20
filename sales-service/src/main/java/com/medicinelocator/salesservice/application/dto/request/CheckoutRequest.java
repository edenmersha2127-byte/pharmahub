package com.medicinelocator.salesservice.application.dto.request;

import com.medicinelocator.salesservice.domain.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class CheckoutRequest {

    /** Optional walk-in customer to attach the sale to (for purchase history). */
    private UUID customerId;

    @NotNull(message = "paymentMethod is required")
    private PaymentMethod paymentMethod;

    @NotEmpty(message = "A sale must contain at least one item")
    @Valid
    private List<CheckoutItemRequest> items;

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public List<CheckoutItemRequest> getItems() { return items; }
    public void setItems(List<CheckoutItemRequest> items) { this.items = items; }
}
