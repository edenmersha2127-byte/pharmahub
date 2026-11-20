package com.medicinelocator.reportservice.domain.exception;

import java.util.UUID;

public class SupportTicketNotFoundException extends RuntimeException {

    public SupportTicketNotFoundException(UUID ticketId) {
        super("Support ticket not found with id: " + ticketId);
    }
}
