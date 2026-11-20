package com.medicinelocator.paymentservice.domain.exception;

/**
 * Raised when the payment gateway (Chapa) call fails or returns an error.
 */
public class PaymentProcessingException extends RuntimeException {

    public PaymentProcessingException(String message) {
        super(message);
    }

    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
