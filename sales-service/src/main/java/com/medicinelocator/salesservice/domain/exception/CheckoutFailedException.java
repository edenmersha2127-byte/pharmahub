package com.medicinelocator.salesservice.domain.exception;

/**
 * Raised when the POS checkout cannot be completed — typically because the
 * inventory service rejected the stock deduction (insufficient stock, unknown
 * medicine) or was unreachable. Carries an HTTP-ish status hint for the handler.
 */
public class CheckoutFailedException extends RuntimeException {

    private final boolean clientError;

    public CheckoutFailedException(String message, boolean clientError) {
        super(message);
        this.clientError = clientError;
    }

    /** True when the failure is the caller's fault (e.g. insufficient stock) → 4xx. */
    public boolean isClientError() {
        return clientError;
    }
}
