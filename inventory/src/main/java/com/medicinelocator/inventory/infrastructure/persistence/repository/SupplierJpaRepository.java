package com.medicinelocator.inventory.infrastructure.persistence.repository;

import com.medicinelocator.inventory.infrastructure.persistence.entity.SupplierEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierJpaRepository extends JpaRepository<SupplierEntity, UUID> {

    Optional<SupplierEntity> findByIdAndPharmacyId(UUID id, UUID pharmacyId);

    List<SupplierEntity> findByPharmacyIdOrderByNameAsc(UUID pharmacyId);
}
