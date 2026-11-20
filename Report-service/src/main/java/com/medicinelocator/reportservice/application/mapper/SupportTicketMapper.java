package com.medicinelocator.reportservice.application.mapper;

import com.medicinelocator.reportservice.application.dto.response.SupportTicketListResponse;
import com.medicinelocator.reportservice.application.dto.response.SupportTicketResponse;
import com.medicinelocator.reportservice.domain.model.SupportTicket;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class SupportTicketMapper {

    public SupportTicketResponse toResponse(SupportTicket ticket) {
        SupportTicketResponse response = new SupportTicketResponse();
        response.setId(ticket.getId());
        response.setReporterId(ticket.getReporterId());
        response.setReporterRole(ticket.getReporterRole());
        response.setCategory(ticket.getCategory());
        response.setSubject(ticket.getSubject());
        response.setDescription(ticket.getDescription());
        response.setStatus(ticket.getStatus());
        response.setAdminResponse(ticket.getAdminResponse());
        response.setResolvedBy(ticket.getResolvedBy());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());
        return response;
    }

    public SupportTicketListResponse toListResponse(Page<SupportTicket> page) {
        return new SupportTicketListResponse(
                page.getContent().stream().map(this::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
