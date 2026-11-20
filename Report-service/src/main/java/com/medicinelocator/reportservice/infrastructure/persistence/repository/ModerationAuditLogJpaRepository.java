package com.medicinelocator.reportservice.infrastructure.persistence.repository;

import com.medicinelocator.reportservice.infrastructure.persistence.entity.ModerationAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ModerationAuditLogJpaRepository
        extends JpaRepository<ModerationAuditLogEntity, UUID> {
}
