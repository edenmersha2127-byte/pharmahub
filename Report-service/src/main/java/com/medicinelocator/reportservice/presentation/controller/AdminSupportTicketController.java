package com.medicinelocator.reportservice.presentation.controller;

import com.medicinelocator.reportservice.application.command.DismissSupportTicketCommand;
import com.medicinelocator.reportservice.application.command.ResolveSupportTicketCommand;
import com.medicinelocator.reportservice.application.command.handler.DismissSupportTicketHandler;
import com.medicinelocator.reportservice.application.command.handler.ResolveSupportTicketHandler;
import com.medicinelocator.reportservice.application.dto.request.ResolveReportRequest;
import com.medicinelocator.reportservice.application.dto.response.SupportTicketListResponse;
import com.medicinelocator.reportservice.application.dto.response.SupportTicketResponse;
import com.medicinelocator.reportservice.application.mapper.SupportTicketMapper;
import com.medicinelocator.reportservice.application.query.GetTicketQueueQuery;
import com.medicinelocator.reportservice.application.query.handler.GetTicketQueueHandler;
import com.medicinelocator.reportservice.domain.enums.ReportStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin support-ticket API. All routes require ROLE_ADMIN or ROLE_REPORT_ADMIN.
 * Mounted under /api/admin/issues so the gateway can route it to this service
 * ahead of the auth service's /api/admin/** catch-all.
 */
@RestController
@RequestMapping("/api/admin/issues")
@Validated
@PreAuthorize("hasAnyRole('ADMIN','REPORT_ADMIN')")
public class AdminSupportTicketController {

    private final GetTicketQueueHandler getTicketQueueHandler;
    private final ResolveSupportTicketHandler resolveSupportTicketHandler;
    private final DismissSupportTicketHandler dismissSupportTicketHandler;
    private final SupportTicketMapper supportTicketMapper;

    public AdminSupportTicketController(GetTicketQueueHandler getTicketQueueHandler,
                                        ResolveSupportTicketHandler resolveSupportTicketHandler,
                                        DismissSupportTicketHandler dismissSupportTicketHandler,
                                        SupportTicketMapper supportTicketMapper) {
        this.getTicketQueueHandler = getTicketQueueHandler;
        this.resolveSupportTicketHandler = resolveSupportTicketHandler;
        this.dismissSupportTicketHandler = dismissSupportTicketHandler;
        this.supportTicketMapper = supportTicketMapper;
    }

    /** The ticket queue, optionally filtered by status. */
    @GetMapping
    public ResponseEntity<SupportTicketListResponse> queue(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        return ResponseEntity.ok(getTicketQueueHandler.handle(
                new GetTicketQueueQuery(status, page, size)));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<SupportTicketResponse> resolve(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable UUID id,
            @RequestBody(required = false) @Valid ResolveReportRequest request) {

        String note = request != null ? request.getNote() : null;
        return ResponseEntity.ok(supportTicketMapper.toResponse(
                resolveSupportTicketHandler.handle(
                        new ResolveSupportTicketCommand(id, parseUserId(userId), role, note))));
    }

    @PostMapping("/{id}/dismiss")
    public ResponseEntity<SupportTicketResponse> dismiss(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable UUID id,
            @RequestBody(required = false) @Valid ResolveReportRequest request) {

        String note = request != null ? request.getNote() : null;
        return ResponseEntity.ok(supportTicketMapper.toResponse(
                dismissSupportTicketHandler.handle(
                        new DismissSupportTicketCommand(id, parseUserId(userId), role, note))));
    }

    private UUID parseUserId(String userId) {
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid or missing X-User-Id header");
        }
    }
}
