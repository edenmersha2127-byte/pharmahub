package com.medicinelocator.salesservice.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        UUID pharmacyId,
        String name,
        String phone,
        String email,
        String note,
        LocalDateTime createdAt
) {
}
