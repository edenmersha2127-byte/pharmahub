package com.medicinelocator.auth.application.service;

import com.medicinelocator.auth.domain.enums.PharmacyStatus;
import com.medicinelocator.auth.domain.model.Pharmacy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PharmacyService {

    Pharmacy save(Pharmacy pharmacy);

    Optional<Pharmacy> findByEmail(String email);

    Optional<Pharmacy> findById(UUID id);

    boolean existsByEmail(String email);

    Pharmacy update(Pharmacy pharmacy);

    Page<Pharmacy> findAll(PharmacyStatus statusFilter, Pageable pageable);

    List<Pharmacy> findAllUnpaged();

    /**
     * Pharmacies still marked SUSPENDED whose suspension window has already
     * elapsed. Used by the auto-lift scheduler.
     */
    List<Pharmacy> findExpiredSuspensions(LocalDateTime now);
}