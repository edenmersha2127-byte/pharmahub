package com.medicinelocator.reportservice.infrastructure.persistence.repository;

import com.medicinelocator.reportservice.domain.enums.ReportStatus;
import com.medicinelocator.reportservice.infrastructure.persistence.entity.SupportTicketEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SupportTicketJpaRepository extends JpaRepository<SupportTicketEntity, UUID> {

    Page<SupportTicketEntity> findByReporterId(UUID reporterId, Pageable pageable);

    Page<SupportTicketEntity> findByStatus(ReportStatus status, Pageable pageable);
}
