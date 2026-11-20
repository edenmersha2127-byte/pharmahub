package com.medicinelocator.prescription.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Admin onboards an integrated hospital. The system generates the unique
 * hospitalId — it is NOT supplied here.
 */
public record CreateHospitalConnectionRequest(

        @NotBlank(message = "Hospital name is required")
        String name,

        @NotBlank(message = "Contact email is required")
        @Email(message = "A valid email is required")
        String email,

        @NotBlank(message = "Hospital base URL is required")
        String baseUrl,

        String apiKey
) {
}
