package com.medicinelocator.prescription.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Admin onboards an integrated hospital. The system generates the unique
 * hospitalId unless the admin supplies one.
 */
public record CreateHospitalConnectionRequest(

        @NotBlank(message = "Hospital name is required")
        String name,

        @NotBlank(message = "Contact email is required")
        @Email(message = "A valid email is required")
        String email,

        @NotBlank(message = "Hospital base URL is required")
        String baseUrl,

        String apiKey,

        /** Optional: when blank, the system generates a unique HSP-XXXXX id. */
        @Size(max = 100, message = "Hospital ID must be at most 100 characters")
        String hospitalId
) {
}
