package com.medicinelocator.prescription.application.dto.response;

import java.util.UUID;

/**
 * Note: the apiKey is intentionally NOT exposed in responses.
 */
public record HospitalConnectionResponse(
        UUID id,
        String name,
        String email,
        String hospitalId,
        String baseUrl,
        boolean active
) {
}
