package com.medicinelocator.auth.application.service;

import com.medicinelocator.auth.domain.model.Staff;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StaffService {

    Staff save(Staff staff);

    Optional<Staff> findByEmail(String email);

    Optional<Staff> findById(UUID id);

    boolean existsByEmail(String email);

    Staff update(Staff staff);

    List<Staff> findByPharmacyId(UUID pharmacyId);
}
