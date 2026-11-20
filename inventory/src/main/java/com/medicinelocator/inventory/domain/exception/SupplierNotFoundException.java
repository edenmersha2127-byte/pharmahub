package com.medicinelocator.inventory.domain.exception;

import java.util.UUID;

public class SupplierNotFoundException extends RuntimeException {

    public SupplierNotFoundException(UUID pharmacyId, UUID supplierId) {
        super("Supplier not found with id: " + supplierId + " for pharmacy: " + pharmacyId);
    }
}
