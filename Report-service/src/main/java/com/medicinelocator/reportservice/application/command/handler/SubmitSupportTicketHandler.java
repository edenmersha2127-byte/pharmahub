package com.medicinelocator.reportservice.application.command.handler;

import com.medicinelocator.reportservice.application.command.SubmitSupportTicketCommand;
import com.medicinelocator.reportservice.application.service.SupportTicketService;
import com.medicinelocator.reportservice.domain.model.SupportTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Any authenticated user (customer, pharmacy, staff or admin) raises a support
 * ticket describing a software/experience issue. The ticket lands in the admin
 * queue as PENDING.
 */
@Component
public class SubmitSupportTicketHandler {

    private static final Logger log = LoggerFactory.getLogger(SubmitSupportTicketHandler.class);

    private final SupportTicketService supportTicketService;

    public SubmitSupportTicketHandler(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @Transactional
    public SupportTicket handle(SubmitSupportTicketCommand command) {
        SupportTicket saved = supportTicketService.save(SupportTicket.create(
                command.getReporterId(),
                command.getReporterRole(),
                command.getCategory(),
                command.getSubject(),
                command.getDescription()));

        log.info("Support ticket raised: id={} reporterId={} role={} category={}",
                saved.getId(), saved.getReporterId(), saved.getReporterRole(), saved.getCategory());

        return saved;
    }
}
