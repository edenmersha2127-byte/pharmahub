package com.medicinelocator.salesservice.domain.exception;

import java.util.UUID;

public class SaleNotFoundException extends RuntimeException {

    public SaleNotFoundException(UUID pharmacyId, UUID saleId) {
        super("Sale not found with id: " + saleId + " for pharmacy: " + pharmacyId);
    }
}
