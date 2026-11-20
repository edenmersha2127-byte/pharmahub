package com.medicinelocator.inventory.infrastructure.persistence.repository;

import com.medicinelocator.inventory.infrastructure.persistence.entity.MedicineBatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MedicineBatchJpaRepository extends JpaRepository<MedicineBatchEntity, UUID> {

    List<MedicineBatchEntity> findByMedicineIdOrderByExpiryDateAsc(UUID medicineId);

    List<MedicineBatchEntity> findByMedicineIdAndQuantityGreaterThanOrderByExpiryDateAsc(
            UUID medicineId, int quantity);

    @Query("""
            SELECT b FROM MedicineBatchEntity b
            WHERE b.pharmacyId = :pharmacyId
              AND b.quantity > 0
              AND b.expiryDate IS NOT NULL
              AND b.expiryDate BETWEEN :from AND :to
            ORDER BY b.expiryDate ASC
            """)
    List<MedicineBatchEntity> findExpiringBetween(@Param("pharmacyId") UUID pharmacyId,
                                                  @Param("from") LocalDate from,
                                                  @Param("to") LocalDate to);

    @Query("""
            SELECT b FROM MedicineBatchEntity b
            WHERE b.pharmacyId = :pharmacyId
              AND b.quantity > 0
              AND b.expiryDate IS NOT NULL
              AND b.expiryDate < :today
            ORDER BY b.expiryDate ASC
            """)
    List<MedicineBatchEntity> findExpired(@Param("pharmacyId") UUID pharmacyId,
                                          @Param("today") LocalDate today);

    /** Expiring batches across ALL pharmacies (for the scheduled expiry scan). */
    @Query("""
            SELECT b FROM MedicineBatchEntity b
            WHERE b.quantity > 0
              AND b.expiryDate IS NOT NULL
              AND b.expiryDate BETWEEN :from AND :to
            ORDER BY b.pharmacyId ASC, b.expiryDate ASC
            """)
    List<MedicineBatchEntity> findAllExpiringBetween(@Param("from") LocalDate from,
                                                     @Param("to") LocalDate to);
}
