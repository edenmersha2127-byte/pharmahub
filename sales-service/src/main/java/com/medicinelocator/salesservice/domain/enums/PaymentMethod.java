package com.medicinelocator.salesservice.domain.enums;

/**
 * How the customer paid at the POS. This records the method only — the
 * sales-service is NOT a payment gateway.
 */
public enum PaymentMethod {
    CASH,
    CARD,
    MOBILE_MONEY
}
