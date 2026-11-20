package com.medicinelocator.reportservice.application.query.handler;

import com.medicinelocator.reportservice.application.dto.response.AuditLogListResponse;
import com.medicinelocator.reportservice.application.dto.response.AuditLogResponse;
import com.medicinelocator.reportservice.application.query.GetAuditLogsQuery;
import com.medicinelocator.reportservice.application.service.ModerationAuditLogRepository;
import com.medicinelocator.reportservice.domain.model.ModerationAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetAuditLogsHandler {

    private final ModerationAuditLogRepository auditLogRepository;

    public GetAuditLogsHandler(ModerationAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public AuditLogListResponse handle(GetAuditLogsQuery query) {
        PageRequest pageable = PageRequest.of(
                query.getPage(), query.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ModerationAuditLog> page = auditLogRepository.findAll(pageable);
        return new AuditLogListResponse(
                page.getContent().stream().map(AuditLogResponse::from).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
