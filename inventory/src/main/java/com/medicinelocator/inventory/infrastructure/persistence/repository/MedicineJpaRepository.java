package com.medicinelocator.inventory.infrastructure.persistence.repository;

import com.medicinelocator.inventory.infrastructure.persistence.entity.MedicineEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicineJpaRepository extends JpaRepository<MedicineEntity, UUID> {

    Optional<MedicineEntity> findByIdAndPharmacyId(UUID id, UUID pharmacyId);

    boolean existsByPharmacyIdAndMedicineNameIgnoreCase(UUID pharmacyId, String medicineName);

    long countByPharmacyIdAndActiveTrue(UUID pharmacyId);

    /** Active medicines with any stock on hand (stockQuantity &gt; 0). */
    long countByPharmacyIdAndActiveTrueAndStockQuantityGreaterThan(UUID pharmacyId, int quantity);

    /** Total units on hand across all active medicines (0 when none). */
    @Query("""
            SELECT COALESCE(SUM(m.stockQuantity), 0)
            FROM MedicineEntity m
            WHERE m.pharmacyId = :pharmacyId
              AND m.active = true
            """)
    long sumStockUnits(@Param("pharmacyId") UUID pharmacyId);

    /** Retail value on hand: Σ price × stockQuantity over active medicines (0 when none). */
    @Query("""
            SELECT COALESCE(SUM(m.price * m.stockQuantity), 0)
            FROM MedicineEntity m
            WHERE m.pharmacyId = :pharmacyId
              AND m.active = true
            """)
    java.math.BigDecimal sumInventoryValue(@Param("pharmacyId") UUID pharmacyId);

    /** Distinct pharmacies that own at least one active medicine (for the daily snapshot job). */
    @Query("SELECT DISTINCT m.pharmacyId FROM MedicineEntity m WHERE m.active = true")
    List<UUID> findDistinctActivePharmacyIds();

    Optional<MedicineEntity> findByPharmacyIdAndBarcodeAndActiveTrue(UUID pharmacyId, String barcode);

    @Query("""
            SELECT m FROM MedicineEntity m
            WHERE m.pharmacyId = :pharmacyId
              AND m.active = true
              AND (LOWER(m.medicineName) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(m.brandName)   LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY m.medicineName ASC
            """)
    List<MedicineEntity> searchByPharmacyAndName(@Param("pharmacyId") UUID pharmacyId,
                                                 @Param("q") String q);

    Page<MedicineEntity> findByPharmacyIdAndActiveTrue(UUID pharmacyId, Pageable pageable);

    Page<MedicineEntity> findByPharmacyIdAndActiveTrueAndAvailableTrue(UUID pharmacyId, Pageable pageable);


    @Query("""
            SELECT m FROM MedicineEntity m
            WHERE m.active = true
            AND (LOWER(m.medicineName) LIKE LOWER(CONCAT('%', CAST(:medicineName AS string), '%')) 
                 OR CAST(:medicineName AS string) IS NULL)
            AND (LOWER(m.brandName) LIKE LOWER(CONCAT('%', CAST(:brandName AS string), '%')) 
                 OR CAST(:brandName AS string) IS NULL)
            AND (LOWER(m.genericName) LIKE LOWER(CONCAT('%', CAST(:genericName AS string), '%')) 
                 OR CAST(:genericName AS string) IS NULL)
            AND (LOWER(m.category) LIKE LOWER(CONCAT('%', CAST(:category AS string), '%')) 
                 OR CAST(:category AS string) IS NULL)
            AND (:availableOnly IS NULL OR m.available = :availableOnly)
            AND (:requiresPrescription IS NULL OR m.requiresPrescription = :requiresPrescription)
            """)
    Page<MedicineEntity> searchAcrossPharmacies(
            @Param("medicineName") String medicineName,
            @Param("brandName") String brandName,
            @Param("genericName") String genericName,
            @Param("category") String category,
            @Param("availableOnly") Boolean availableOnly,
            @Param("requiresPrescription") Boolean requiresPrescription,
            Pageable pageable
    );

    @Query("""
            SELECT m FROM MedicineEntity m
            WHERE m.pharmacyId = :pharmacyId
              AND m.active = true
              AND m.stockQuantity <= m.minimumStockLevel
            ORDER BY m.stockQuantity ASC
            """)
    List<MedicineEntity> findLowStock(@Param("pharmacyId") UUID pharmacyId);

    @Query("""
            SELECT m FROM MedicineEntity m
            WHERE m.pharmacyId = :pharmacyId
              AND m.active = true
              AND m.expiryDate IS NOT NULL
              AND m.expiryDate BETWEEN :from AND :to
            ORDER BY m.expiryDate ASC
            """)
    List<MedicineEntity> findExpiringBetween(@Param("pharmacyId") UUID pharmacyId,
                                             @Param("from") LocalDate from,
                                             @Param("to") LocalDate to);

    @Query("""
            SELECT m FROM MedicineEntity m
            WHERE m.pharmacyId = :pharmacyId
              AND m.active = true
              AND m.expiryDate IS NOT NULL
              AND m.expiryDate < :today
            ORDER BY m.expiryDate ASC
            """)
    List<MedicineEntity> findExpired(@Param("pharmacyId") UUID pharmacyId,
                                     @Param("today") LocalDate today);
}