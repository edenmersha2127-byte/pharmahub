package com.medicinelocator.prescription.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Admin edits an integrated hospital. Unlike onboarding, the hospitalId is
 * supplied here so an admin can correct it to match what the hospital uses.
 *
 * apiKey: null or blank keeps the stored key (it is never sent to the client,
 * so the edit form cannot echo it back). active: null keeps the current state.
 */
public record UpdateHospitalConnectionRequest(

        @NotBlank(message = "Hospital name is required")
        String name,

        @NotBlank(message = "Contact email is required")
        @Email(message = "A valid email is required")
        String email,

        @NotBlank(message = "Hospital ID is required")
        @Size(max = 100, message = "Hospital ID must be at most 100 characters")
        String hospitalId,

        @NotBlank(message = "Hospital base URL is required")
        String baseUrl,

        String apiKey,

        Boolean active
) {
}
