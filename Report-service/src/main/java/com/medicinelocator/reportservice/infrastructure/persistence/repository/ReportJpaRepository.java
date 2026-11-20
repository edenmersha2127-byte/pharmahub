package com.medicinelocator.reportservice.infrastructure.persistence.repository;

import com.medicinelocator.reportservice.domain.enums.ReportStatus;
import com.medicinelocator.reportservice.infrastructure.persistence.entity.ReportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReportJpaRepository extends JpaRepository<ReportEntity, UUID> {

    Page<ReportEntity> findByReporterId(UUID reporterId, Pageable pageable);

    Page<ReportEntity> findByPharmacyId(UUID pharmacyId, Pageable pageable);

    Page<ReportEntity> findByStatus(ReportStatus status, Pageable pageable);

    /** Total lifetime reports against a pharmacy (all statuses). */
    long countByPharmacyId(UUID pharmacyId);

    boolean existsByReporterIdAndPharmacyIdAndCreatedAtAfter(
            UUID reporterId, UUID pharmacyId, LocalDateTime since);

    @Query("""
            SELECT COUNT(r) FROM ReportEntity r
            WHERE r.pharmacyId = :pharmacyId
              AND r.status IN (com.medicinelocator.reportservice.domain.enums.ReportStatus.PENDING,
                               com.medicinelocator.reportservice.domain.enums.ReportStatus.AWAITING_PHARMACY_RESPONSE,
                               com.medicinelocator.reportservice.domain.enums.ReportStatus.UNDER_REVIEW)
              AND r.createdAt >= :since
            """)
    long countUnresolvedForPharmacySince(@Param("pharmacyId") UUID pharmacyId,
                                         @Param("since") LocalDateTime since);

    /**
     * Pharmacies with at least {@code minUnresolved} open reports since
     * {@code since}, most-reported first. Projected into {@link PharmacyReportCountView}.
     */
    @Query("""
            SELECT r.pharmacyId AS pharmacyId, COUNT(r) AS unresolvedCount
            FROM ReportEntity r
            WHERE r.status IN (com.medicinelocator.reportservice.domain.enums.ReportStatus.PENDING,
                               com.medicinelocator.reportservice.domain.enums.ReportStatus.AWAITING_PHARMACY_RESPONSE,
                               com.medicinelocator.reportservice.domain.enums.ReportStatus.UNDER_REVIEW)
              AND r.createdAt >= :since
            GROUP BY r.pharmacyId
            HAVING COUNT(r) >= :minUnresolved
            ORDER BY COUNT(r) DESC
            """)
    List<PharmacyReportCountView> findFlaggedPharmacies(@Param("since") LocalDateTime since,
                                                        @Param("minUnresolved") long minUnresolved);
}
