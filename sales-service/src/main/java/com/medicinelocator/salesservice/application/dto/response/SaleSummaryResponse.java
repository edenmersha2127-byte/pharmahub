package com.medicinelocator.salesservice.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lightweight sale header for lists and purchase history (no line items).
 */
public record SaleSummaryResponse(
        UUID saleId,
        UUID customerId,
        String paymentMethod,
        BigDecimal total,
        LocalDateTime createdAt
) {
}
