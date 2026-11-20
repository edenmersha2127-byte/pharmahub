package com.medicinelocator.salesservice.infrastructure.persistence.repository;

import com.medicinelocator.salesservice.infrastructure.persistence.entity.PharmacyCustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PharmacyCustomerJpaRepository extends JpaRepository<PharmacyCustomerEntity, UUID> {

    Optional<PharmacyCustomerEntity> findByIdAndPharmacyId(UUID id, UUID pharmacyId);

    List<PharmacyCustomerEntity> findByPharmacyIdOrderByCreatedAtDesc(UUID pharmacyId);
}
