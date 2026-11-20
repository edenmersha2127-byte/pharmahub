package com.medicinelocator.prescription.application.service;

import com.medicinelocator.prescription.domain.model.HospitalConnection;

import java.util.List;
import java.util.Optional;

/**
 * Port for the integrated-hospitals directory.
 */
public interface HospitalConnectionRepository {

    HospitalConnection save(HospitalConnection connection);

    Optional<HospitalConnection> findByHospitalId(String hospitalId);

    List<HospitalConnection> findAll();

    boolean existsByHospitalId(String hospitalId);
}
