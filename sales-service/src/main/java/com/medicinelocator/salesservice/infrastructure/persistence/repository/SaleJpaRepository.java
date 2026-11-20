package com.medicinelocator.salesservice.infrastructure.persistence.repository;

import com.medicinelocator.salesservice.infrastructure.persistence.entity.SaleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SaleJpaRepository extends JpaRepository<SaleEntity, UUID> {

    Optional<SaleEntity> findByIdAndPharmacyId(UUID id, UUID pharmacyId);

    List<SaleEntity> findByPharmacyIdOrderByCreatedAtDesc(UUID pharmacyId);

    List<SaleEntity> findByCustomerIdAndPharmacyIdOrderByCreatedAtDesc(UUID customerId, UUID pharmacyId);

    List<SaleEntity> findByPharmacyIdAndCreatedAtBetween(UUID pharmacyId, LocalDateTime from, LocalDateTime to);
}
