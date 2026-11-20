package com.medicinelocator.reportservice.application.command.handler;

import com.medicinelocator.reportservice.application.command.DismissSupportTicketCommand;
import com.medicinelocator.reportservice.application.service.SupportTicketService;
import com.medicinelocator.reportservice.domain.exception.SupportTicketNotFoundException;
import com.medicinelocator.reportservice.domain.model.SupportTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin dismisses a support ticket (not actionable / not an issue).
 */
@Component
public class DismissSupportTicketHandler {

    private static final Logger log = LoggerFactory.getLogger(DismissSupportTicketHandler.class);

    private final SupportTicketService supportTicketService;

    public DismissSupportTicketHandler(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @Transactional
    public SupportTicket handle(DismissSupportTicketCommand command) {
        SupportTicket ticket = supportTicketService.findById(command.getTicketId())
                .orElseThrow(() -> new SupportTicketNotFoundException(command.getTicketId()));

        ticket.dismiss(command.getAdminId(), command.getNote());
        SupportTicket saved = supportTicketService.save(ticket);

        log.info("Support ticket {} dismissed by admin {}", command.getTicketId(), command.getAdminId());

        return saved;
    }
}
