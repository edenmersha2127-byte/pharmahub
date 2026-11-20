package com.medicinelocator.reportservice.presentation.controller;

import com.medicinelocator.reportservice.application.command.SubmitSupportTicketCommand;
import com.medicinelocator.reportservice.application.command.handler.SubmitSupportTicketHandler;
import com.medicinelocator.reportservice.application.dto.request.SubmitTicketRequest;
import com.medicinelocator.reportservice.application.dto.response.SupportTicketListResponse;
import com.medicinelocator.reportservice.application.dto.response.SupportTicketResponse;
import com.medicinelocator.reportservice.application.mapper.SupportTicketMapper;
import com.medicinelocator.reportservice.application.query.GetMyTicketsQuery;
import com.medicinelocator.reportservice.application.query.handler.GetMyTicketsHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Support-ticket API for reporting software/experience issues to the admins.
 * Open to any authenticated user (customer, pharmacy, pharmacy staff or admin);
 * SecurityConfig already requires an authenticated principal for /api/**. The
 * caller's identity comes from the gateway X-User-Id / X-User-Role headers.
 */
@RestController
@RequestMapping("/api/issues")
@Validated
public class SupportTicketController {

    private final SubmitSupportTicketHandler submitSupportTicketHandler;
    private final GetMyTicketsHandler getMyTicketsHandler;
    private final SupportTicketMapper supportTicketMapper;

    public SupportTicketController(SubmitSupportTicketHandler submitSupportTicketHandler,
                                   GetMyTicketsHandler getMyTicketsHandler,
                                   SupportTicketMapper supportTicketMapper) {
        this.submitSupportTicketHandler = submitSupportTicketHandler;
        this.getMyTicketsHandler = getMyTicketsHandler;
        this.supportTicketMapper = supportTicketMapper;
    }

    /** Raise a support ticket. */
    @PostMapping
    public ResponseEntity<SupportTicketResponse> submitTicket(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody SubmitTicketRequest request) {

        SupportTicketResponse response = supportTicketMapper.toResponse(
                submitSupportTicketHandler.handle(new SubmitSupportTicketCommand(
                        parseUserId(userId),
                        role,
                        request.getCategory(),
                        request.getSubject(),
                        request.getDescription())));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** List the support tickets the current user has raised. */
    @GetMapping("/mine")
    public ResponseEntity<SupportTicketListResponse> myTickets(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        return ResponseEntity.ok(getMyTicketsHandler.handle(
                new GetMyTicketsQuery(parseUserId(userId), page, size)));
    }

    private UUID parseUserId(String userId) {
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid or missing X-User-Id header");
        }
    }
}
