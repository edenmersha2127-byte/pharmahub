package com.medicinelocator.reportservice.infrastructure.persistence.repository;

import com.medicinelocator.reportservice.application.service.PharmacyReportTally;
import com.medicinelocator.reportservice.application.service.ReportRepository;
import com.medicinelocator.reportservice.domain.enums.ReportStatus;
import com.medicinelocator.reportservice.domain.model.Report;
import com.medicinelocator.reportservice.infrastructure.persistence.entity.ReportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ReportRepositoryImpl implements ReportRepository {

    private final ReportJpaRepository jpaRepository;

    public ReportRepositoryImpl(ReportJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Report save(Report report) {
        return toDomain(jpaRepository.save(toEntity(report)));
    }

    @Override
    public Optional<Report> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Page<Report> findByReporter(UUID reporterId, Pageable pageable) {
        return jpaRepository.findByReporterId(reporterId, pageable).map(this::toDomain);
    }

    @Override
    public Page<Report> findByPharmacy(UUID pharmacyId, Pageable pageable) {
        return jpaRepository.findByPharmacyId(pharmacyId, pageable).map(this::toDomain);
    }

    @Override
    public Page<Report> findByStatus(ReportStatus status, Pageable pageable) {
        if (status != null) {
            return jpaRepository.findByStatus(status, pageable).map(this::toDomain);
        }
        return jpaRepository.findAll(pageable).map(this::toDomain);
    }

    @Override
    public boolean existsRecentReport(UUID reporterId, UUID pharmacyId, LocalDateTime since) {
        return jpaRepository.existsByReporterIdAndPharmacyIdAndCreatedAtAfter(
                reporterId, pharmacyId, since);
    }

    @Override
    public long countUnresolvedForPharmacySince(UUID pharmacyId, LocalDateTime since) {
        return jpaRepository.countUnresolvedForPharmacySince(pharmacyId, since);
    }

    @Override
    public long countByPharmacy(UUID pharmacyId) {
        return jpaRepository.countByPharmacyId(pharmacyId);
    }

    @Override
    public List<PharmacyReportTally> findFlaggedPharmacies(LocalDateTime since, long minUnresolved) {
        return jpaRepository.findFlaggedPharmacies(since, minUnresolved).stream()
                .map(v -> new PharmacyReportTally(v.getPharmacyId(), v.getUnresolvedCount()))
                .toList();
    }

    private ReportEntity toEntity(Report report) {
        ReportEntity entity = new ReportEntity();
        entity.setId(report.getId());
        entity.setReporterId(report.getReporterId());
        entity.setPharmacyId(report.getPharmacyId());
        entity.setCategory(report.getCategory());
        entity.setDescription(report.getDescription());
        entity.setStatus(report.getStatus());
        entity.setPharmacyResponse(report.getPharmacyResponse());
        entity.setResolutionNote(report.getResolutionNote());
        entity.setResolvedBy(report.getResolvedBy());
        entity.setCreatedAt(report.getCreatedAt());
        entity.setUpdatedAt(report.getUpdatedAt());
        return entity;
    }

    private Report toDomain(ReportEntity entity) {
        return new Report(
                entity.getId(),
                entity.getReporterId(),
                entity.getPharmacyId(),
                entity.getCategory(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getPharmacyResponse(),
                entity.getResolutionNote(),
                entity.getResolvedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
