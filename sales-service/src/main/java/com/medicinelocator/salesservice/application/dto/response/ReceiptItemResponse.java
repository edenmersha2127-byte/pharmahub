package com.medicinelocator.salesservice.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ReceiptItemResponse(
        UUID medicineId,
        String medicineName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
