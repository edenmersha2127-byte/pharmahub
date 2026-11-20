package com.medicinelocator.prescription.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * One medicine extracted from a prescription, resolved against the dispensing
 * pharmacy's own catalogue. When {@code matched} is false the name was read off
 * the prescription but no catalogue item matched, so the sale fields are null.
 */
public record ResolvedMedicineResponse(
        String extractedName,
        boolean matched,
        UUID medicineId,
        String medicineName,
        BigDecimal price,
        Integer stockQuantity,
        boolean inStock
) {
}
