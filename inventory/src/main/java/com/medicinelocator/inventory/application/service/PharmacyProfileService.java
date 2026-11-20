package com.medicinelocator.inventory.application.service;

import com.medicinelocator.inventory.application.dto.response.PharmacyProfileResponse;

import java.util.Optional;
import java.util.UUID;

/**
 * Application-layer port for fetching pharmacy profile data.
 *
 * ONLY ONE implementation exists: AuthServiceRestClient.
 * The pharmacy profile data is owned by the Auth Service.
 * This service must NEVER attempt to read from a local pharmacy_profiles table.
 */
public interface PharmacyProfileService {

    Optional<PharmacyProfileResponse> findById(UUID pharmacyId);
}