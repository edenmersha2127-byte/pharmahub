package com.medicinelocator.reportservice.infrastructure.persistence.repository;

import com.medicinelocator.reportservice.application.service.SupportTicketService;
import com.medicinelocator.reportservice.domain.enums.ReportStatus;
import com.medicinelocator.reportservice.domain.model.SupportTicket;
import com.medicinelocator.reportservice.infrastructure.persistence.entity.SupportTicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SupportTicketRepositoryImpl implements SupportTicketService {

    private final SupportTicketJpaRepository jpaRepository;

    public SupportTicketRepositoryImpl(SupportTicketJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SupportTicket save(SupportTicket ticket) {
        return toDomain(jpaRepository.save(toEntity(ticket)));
    }

    @Override
    public Optional<SupportTicket> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Page<SupportTicket> findByReporterId(UUID reporterId, Pageable pageable) {
        return jpaRepository.findByReporterId(reporterId, pageable).map(this::toDomain);
    }

    @Override
    public Page<SupportTicket> findByStatus(ReportStatus status, Pageable pageable) {
        if (status != null) {
            return jpaRepository.findByStatus(status, pageable).map(this::toDomain);
        }
        return jpaRepository.findAll(pageable).map(this::toDomain);
    }

    private SupportTicketEntity toEntity(SupportTicket ticket) {
        SupportTicketEntity entity = new SupportTicketEntity();
        entity.setId(ticket.getId());
        entity.setReporterId(ticket.getReporterId());
        entity.setReporterRole(ticket.getReporterRole());
        entity.setCategory(ticket.getCategory());
        entity.setSubject(ticket.getSubject());
        entity.setDescription(ticket.getDescription());
        entity.setStatus(ticket.getStatus());
        entity.setAdminResponse(ticket.getAdminResponse());
        entity.setResolvedBy(ticket.getResolvedBy());
        entity.setCreatedAt(ticket.getCreatedAt());
        entity.setUpdatedAt(ticket.getUpdatedAt());
        return entity;
    }

    private SupportTicket toDomain(SupportTicketEntity entity) {
        return new SupportTicket(
                entity.getId(),
                entity.getReporterId(),
                entity.getReporterRole(),
                entity.getCategory(),
                entity.getSubject(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getAdminResponse(),
                entity.getResolvedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
