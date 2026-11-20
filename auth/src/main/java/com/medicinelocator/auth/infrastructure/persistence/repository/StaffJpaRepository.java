package com.medicinelocator.auth.infrastructure.persistence.repository;

import com.medicinelocator.auth.infrastructure.persistence.entity.StaffEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StaffJpaRepository extends JpaRepository<StaffEntity, UUID> {

    Optional<StaffEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<StaffEntity> findByPharmacyId(UUID pharmacyId);
}
