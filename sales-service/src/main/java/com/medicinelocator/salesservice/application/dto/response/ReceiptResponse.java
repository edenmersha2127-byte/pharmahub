package com.medicinelocator.salesservice.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Full sale record / printable receipt.
 */
public record ReceiptResponse(
        UUID saleId,
        UUID pharmacyId,
        UUID customerId,
        UUID cashierId,
        String paymentMethod,
        BigDecimal total,
        LocalDateTime createdAt,
        List<ReceiptItemResponse> items
) {
}
