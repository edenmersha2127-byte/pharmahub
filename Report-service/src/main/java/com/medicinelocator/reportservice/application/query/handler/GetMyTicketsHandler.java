package com.medicinelocator.reportservice.application.query.handler;

import com.medicinelocator.reportservice.application.dto.response.SupportTicketListResponse;
import com.medicinelocator.reportservice.application.mapper.SupportTicketMapper;
import com.medicinelocator.reportservice.application.query.GetMyTicketsQuery;
import com.medicinelocator.reportservice.application.service.SupportTicketService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetMyTicketsHandler {

    private final SupportTicketService supportTicketService;
    private final SupportTicketMapper supportTicketMapper;

    public GetMyTicketsHandler(SupportTicketService supportTicketService,
                               SupportTicketMapper supportTicketMapper) {
        this.supportTicketService = supportTicketService;
        this.supportTicketMapper = supportTicketMapper;
    }

    @Transactional(readOnly = true)
    public SupportTicketListResponse handle(GetMyTicketsQuery query) {
        PageRequest pageable = PageRequest.of(
                query.getPage(), query.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        return supportTicketMapper.toListResponse(
                supportTicketService.findByReporterId(query.getReporterId(), pageable));
    }
}
