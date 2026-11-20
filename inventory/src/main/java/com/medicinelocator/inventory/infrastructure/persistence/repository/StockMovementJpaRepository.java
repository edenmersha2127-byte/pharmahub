package com.medicinelocator.inventory.infrastructure.persistence.repository;

import com.medicinelocator.inventory.infrastructure.persistence.entity.StockMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StockMovementJpaRepository extends JpaRepository<StockMovementEntity, UUID> {

    List<StockMovementEntity> findByMedicineIdAndPharmacyIdOrderByCreatedAtDesc(
            UUID medicineId, UUID pharmacyId);

    /** All movements for a pharmacy within a time window (for the movement-summary chart). */
    List<StockMovementEntity> findByPharmacyIdAndCreatedAtBetweenOrderByCreatedAtAsc(
            UUID pharmacyId, LocalDateTime from, LocalDateTime to);
}
