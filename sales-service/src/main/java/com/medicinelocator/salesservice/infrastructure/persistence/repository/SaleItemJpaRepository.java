package com.medicinelocator.salesservice.infrastructure.persistence.repository;

import com.medicinelocator.salesservice.infrastructure.persistence.entity.SaleItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SaleItemJpaRepository extends JpaRepository<SaleItemEntity, UUID> {

    List<SaleItemEntity> findBySaleId(UUID saleId);

    /** All sale-item lines for a pharmacy within a time window (for analytics). */
    @Query("""
            SELECT i FROM SaleItemEntity i
            WHERE i.saleId IN (
                SELECT s.id FROM SaleEntity s
                WHERE s.pharmacyId = :pharmacyId
                  AND s.createdAt BETWEEN :from AND :to
            )
            """)
    List<SaleItemEntity> findItemsBetween(@Param("pharmacyId") UUID pharmacyId,
                                          @Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to);
}
