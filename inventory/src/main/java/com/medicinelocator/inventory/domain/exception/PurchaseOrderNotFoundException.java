package com.medicinelocator.inventory.domain.exception;

import java.util.UUID;

public class PurchaseOrderNotFoundException extends RuntimeException {

    public PurchaseOrderNotFoundException(UUID pharmacyId, UUID purchaseOrderId) {
        super("Purchase order not found with id: " + purchaseOrderId + " for pharmacy: " + pharmacyId);
    }
}
