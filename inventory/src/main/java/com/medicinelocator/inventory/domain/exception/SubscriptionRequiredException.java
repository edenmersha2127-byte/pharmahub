package com.medicinelocator.inventory.domain.exception;

/**
 * Thrown when a pharmacy without an active subscription tries to modify its
 * inventory. Surfaced as HTTP 403.
 */
public class SubscriptionRequiredException extends RuntimeException {

    public SubscriptionRequiredException(String message) {
        super(message);
    }
}
