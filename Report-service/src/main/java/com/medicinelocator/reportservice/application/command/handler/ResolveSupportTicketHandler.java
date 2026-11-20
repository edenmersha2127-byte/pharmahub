package com.medicinelocator.reportservice.application.command.handler;

import com.medicinelocator.reportservice.application.command.ResolveSupportTicketCommand;
import com.medicinelocator.reportservice.application.service.SupportTicketService;
import com.medicinelocator.reportservice.domain.exception.SupportTicketNotFoundException;
import com.medicinelocator.reportservice.domain.model.SupportTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin resolves a support ticket (issue addressed).
 */
@Component
public class ResolveSupportTicketHandler {

    private static final Logger log = LoggerFactory.getLogger(ResolveSupportTicketHandler.class);

    private final SupportTicketService supportTicketService;

    public ResolveSupportTicketHandler(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @Transactional
    public SupportTicket handle(ResolveSupportTicketCommand command) {
        SupportTicket ticket = supportTicketService.findById(command.getTicketId())
                .orElseThrow(() -> new SupportTicketNotFoundException(command.getTicketId()));

        ticket.resolve(command.getAdminId(), command.getNote());
        SupportTicket saved = supportTicketService.save(ticket);

        log.info("Support ticket {} resolved by admin {}", command.getTicketId(), command.getAdminId());

        return saved;
    }
}
