package com.medicinelocator.reportservice.application.service;

import com.medicinelocator.reportservice.domain.enums.ReportStatus;
import com.medicinelocator.reportservice.domain.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application-layer persistence port for reports.
 * Implemented by the infrastructure persistence adapter.
 */
public interface ReportRepository {

    Report save(Report report);

    Optional<Report> findById(UUID id);

    Page<Report> findByReporter(UUID reporterId, Pageable pageable);

    Page<Report> findByPharmacy(UUID pharmacyId, Pageable pageable);

    /** Admin queue. When {@code status} is null, returns every report. */
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    /**
     * Anti-spam guard: has this customer already reported this pharmacy since
     * the given instant?
     */
    boolean existsRecentReport(UUID reporterId, UUID pharmacyId, LocalDateTime since);

    /** Number of a pharmacy's still-open reports filed since the given instant. */
    long countUnresolvedForPharmacySince(UUID pharmacyId, LocalDateTime since);

    /** Total lifetime reports against a pharmacy, regardless of status. */
    long countByPharmacy(UUID pharmacyId);

    /**
     * Pharmacies whose unresolved-report count since {@code since} is at least
     * {@code minUnresolved}, ordered most-reported first (flagged dashboard).
     */
    List<PharmacyReportTally> findFlaggedPharmacies(LocalDateTime since, long minUnresolved);
}
