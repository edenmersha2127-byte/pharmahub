package com.medicinelocator.inventory.infrastructure.persistence.repository;

import com.medicinelocator.inventory.infrastructure.persistence.entity.InventorySnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventorySnapshotJpaRepository extends JpaRepository<InventorySnapshotEntity, UUID> {

    boolean existsByPharmacyIdAndSnapshotDate(UUID pharmacyId, LocalDate snapshotDate);

    /** The most recent snapshot for a pharmacy on or before the given date (the trend baseline). */
    Optional<InventorySnapshotEntity> findTopByPharmacyIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(
            UUID pharmacyId, LocalDate snapshotDate);
}
