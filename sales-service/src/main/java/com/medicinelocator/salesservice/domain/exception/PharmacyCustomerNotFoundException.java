package com.medicinelocator.salesservice.domain.exception;

import java.util.UUID;

public class PharmacyCustomerNotFoundException extends RuntimeException {

    public PharmacyCustomerNotFoundException(UUID pharmacyId, UUID customerId) {
        super("Customer not found with id: " + customerId + " for pharmacy: " + pharmacyId);
    }
}
