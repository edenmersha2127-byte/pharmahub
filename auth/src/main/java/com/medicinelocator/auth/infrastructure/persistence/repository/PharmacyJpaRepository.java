package com.medicinelocator.auth.infrastructure.persistence.repository;

import com.medicinelocator.auth.domain.enums.AccountStatus;
import com.medicinelocator.auth.domain.enums.PharmacyStatus;
import com.medicinelocator.auth.infrastructure.persistence.entity.PharmacyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PharmacyJpaRepository extends JpaRepository<PharmacyEntity, UUID> {

    Optional<PharmacyEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<PharmacyEntity> findByPharmacyStatus(PharmacyStatus pharmacyStatus, Pageable pageable);

    List<PharmacyEntity> findByAccountStatusAndSuspendedUntilBefore(
            AccountStatus accountStatus, LocalDateTime cutoff);
}