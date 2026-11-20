package com.medicinelocator.prescription.infrastructure.persistence.repository;

import com.medicinelocator.prescription.infrastructure.persistence.entity.HospitalConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HospitalConnectionJpaRepository extends JpaRepository<HospitalConnectionEntity, UUID> {

    Optional<HospitalConnectionEntity> findByHospitalId(String hospitalId);

    boolean existsByHospitalId(String hospitalId);
}
