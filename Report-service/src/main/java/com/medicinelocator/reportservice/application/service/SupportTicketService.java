package com.medicinelocator.reportservice.application.service;

import com.medicinelocator.reportservice.domain.enums.ReportStatus;
import com.medicinelocator.reportservice.domain.model.SupportTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Application-layer persistence port for support tickets.
 * Implemented by the infrastructure persistence adapter.
 */
public interface SupportTicketService {

    SupportTicket save(SupportTicket ticket);

    Optional<SupportTicket> findById(UUID id);

    Page<SupportTicket> findByReporterId(UUID reporterId, Pageable pageable);

    /** Admin queue. When {@code status} is null, returns every ticket. */
    Page<SupportTicket> findByStatus(ReportStatus status, Pageable pageable);
}
