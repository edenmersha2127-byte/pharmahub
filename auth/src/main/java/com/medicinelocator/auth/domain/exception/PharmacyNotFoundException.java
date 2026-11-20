package com.medicinelocator.auth.domain.exception;

import java.util.UUID;

public class PharmacyNotFoundException extends RuntimeException {

    public PharmacyNotFoundException(UUID id) {
        super("Pharmacy not found with id: " + id);
    }

    public PharmacyNotFoundException(String message) {
        super(message);
    }
}