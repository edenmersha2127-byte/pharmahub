package com.medicinelocator.salesservice.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A best-selling medicine over a period: units sold and revenue generated.
 */
public record TopMedicineResponse(
        UUID medicineId,
        String medicineName,
        long quantitySold,
        BigDecimal revenue
) {
}
