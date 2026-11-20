package com.medicinelocator.paymentservice.application.service;

import java.math.BigDecimal;

/**
 * Application-layer port for the external payment gateway (Chapa). The only
 * implementation is a REST client.
 */
public interface PaymentGateway {

    /** Initialize a hosted checkout; returns the URL the pharmacy is sent to. */
    String initializeCheckout(CheckoutRequest request);

    /** Server-side verification of a transaction by our tx_ref. */
    VerificationResult verify(String txRef);

    /** Data needed to open a Chapa checkout. */
    record CheckoutRequest(
            String txRef,
            BigDecimal amount,
            String email,
            String firstName,
            String lastName) {
    }

    /** Outcome of verifying a transaction. */
    record VerificationResult(
            boolean success,
            String providerStatus,
            String rawPayload) {
    }
}
