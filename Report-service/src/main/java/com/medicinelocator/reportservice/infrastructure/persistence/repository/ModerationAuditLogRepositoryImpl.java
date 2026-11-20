package com.medicinelocator.reportservice.infrastructure.persistence.repository;

import com.medicinelocator.reportservice.application.service.ModerationAuditLogRepository;
import com.medicinelocator.reportservice.domain.model.ModerationAuditLog;
import com.medicinelocator.reportservice.infrastructure.persistence.entity.ModerationAuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ModerationAuditLogRepositoryImpl implements ModerationAuditLogRepository {

    private final ModerationAuditLogJpaRepository jpaRepository;

    public ModerationAuditLogRepositoryImpl(ModerationAuditLogJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ModerationAuditLog save(ModerationAuditLog logEntry) {
        return toDomain(jpaRepository.save(toEntity(logEntry)));
    }

    @Override
    public Page<ModerationAuditLog> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(this::toDomain);
    }

    private ModerationAuditLogEntity toEntity(ModerationAuditLog logEntry) {
        ModerationAuditLogEntity entity = new ModerationAuditLogEntity();
        entity.setId(logEntry.getId());
        entity.setAction(logEntry.getAction());
        entity.setActorId(logEntry.getActorId());
        entity.setActorRole(logEntry.getActorRole());
        entity.setReportId(logEntry.getReportId());
        entity.setPharmacyId(logEntry.getPharmacyId());
        entity.setDetails(logEntry.getDetails());
        entity.setCreatedAt(logEntry.getCreatedAt());
        return entity;
    }

    private ModerationAuditLog toDomain(ModerationAuditLogEntity entity) {
        return new ModerationAuditLog(
                entity.getId(),
                entity.getAction(),
                entity.getActorId(),
                entity.getActorRole(),
                entity.getReportId(),
                entity.getPharmacyId(),
                entity.getDetails(),
                entity.getCreatedAt()
        );
    }
}
