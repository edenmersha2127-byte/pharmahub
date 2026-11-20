package com.medicinelocator.reportservice.application.service;

import com.medicinelocator.reportservice.domain.model.ModerationAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Application-layer persistence port for the moderation audit log.
 */
public interface ModerationAuditLogRepository {

    ModerationAuditLog save(ModerationAuditLog logEntry);

    Page<ModerationAuditLog> findAll(Pageable pageable);
}
