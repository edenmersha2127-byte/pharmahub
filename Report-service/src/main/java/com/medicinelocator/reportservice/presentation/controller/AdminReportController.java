package com.medicinelocator.reportservice.presentation.controller;

import com.medicinelocator.reportservice.application.command.DismissReportCommand;
import com.medicinelocator.reportservice.application.command.LiftSuspensionCommand;
import com.medicinelocator.reportservice.application.command.ResolveReportCommand;
import com.medicinelocator.reportservice.application.command.SuspendPharmacyCommand;
import com.medicinelocator.reportservice.application.command.WarnPharmacyCommand;
import com.medicinelocator.reportservice.application.command.handler.DismissReportHandler;
import com.medicinelocator.reportservice.application.command.handler.LiftSuspensionHandler;
import com.medicinelocator.reportservice.application.command.handler.ResolveReportHandler;
import com.medicinelocator.reportservice.application.command.handler.SuspendPharmacyHandler;
import com.medicinelocator.reportservice.application.command.handler.WarnPharmacyHandler;
import com.medicinelocator.reportservice.application.dto.request.ResolveReportRequest;
import com.medicinelocator.reportservice.application.dto.request.SuspendPharmacyRequest;
import com.medicinelocator.reportservice.application.dto.request.WarnPharmacyRequest;
import com.medicinelocator.reportservice.application.dto.response.AuditLogListResponse;
import com.medicinelocator.reportservice.application.dto.response.FlaggedPharmacyResponse;
import com.medicinelocator.reportservice.application.dto.response.ReportListResponse;
import com.medicinelocator.reportservice.application.dto.response.ReportResponse;
import com.medicinelocator.reportservice.application.mapper.ReportMapper;
import com.medicinelocator.reportservice.application.query.GetAuditLogsQuery;
import com.medicinelocator.reportservice.application.query.GetFlaggedPharmaciesQuery;
import com.medicinelocator.reportservice.application.query.GetReportQueueQuery;
import com.medicinelocator.reportservice.application.query.handler.GetAuditLogsHandler;
import com.medicinelocator.reportservice.application.query.handler.GetFlaggedPharmaciesHandler;
import com.medicinelocator.reportservice.application.query.handler.GetReportQueueHandler;
import com.medicinelocator.reportservice.domain.enums.ReportStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin moderation API. All routes require ROLE_ADMIN. Mounted under
 * /api/admin/reports so the gateway can route it to this service ahead of the
 * auth service's /api/admin/** catch-all.
 */
@RestController
@RequestMapping("/api/admin/reports")
@Validated
@PreAuthorize("hasAnyRole('ADMIN','REPORT_ADMIN')")
public class AdminReportController {

    private final GetReportQueueHandler getReportQueueHandler;
    private final GetFlaggedPharmaciesHandler getFlaggedPharmaciesHandler;
    private final GetAuditLogsHandler getAuditLogsHandler;
    private final ResolveReportHandler resolveReportHandler;
    private final DismissReportHandler dismissReportHandler;
    private final WarnPharmacyHandler warnPharmacyHandler;
    private final SuspendPharmacyHandler suspendPharmacyHandler;
    private final LiftSuspensionHandler liftSuspensionHandler;
    private final ReportMapper reportMapper;

    public AdminReportController(GetReportQueueHandler getReportQueueHandler,
                                 GetFlaggedPharmaciesHandler getFlaggedPharmaciesHandler,
                                 GetAuditLogsHandler getAuditLogsHandler,
                                 ResolveReportHandler resolveReportHandler,
                                 DismissReportHandler dismissReportHandler,
                                 WarnPharmacyHandler warnPharmacyHandler,
                                 SuspendPharmacyHandler suspendPharmacyHandler,
                                 LiftSuspensionHandler liftSuspensionHandler,
                                 ReportMapper reportMapper) {
        this.getReportQueueHandler = getReportQueueHandler;
        this.getFlaggedPharmaciesHandler = getFlaggedPharmaciesHandler;
        this.getAuditLogsHandler = getAuditLogsHandler;
        this.resolveReportHandler = resolveReportHandler;
        this.dismissReportHandler = dismissReportHandler;
        this.warnPharmacyHandler = warnPharmacyHandler;
        this.suspendPharmacyHandler = suspendPharmacyHandler;
        this.liftSuspensionHandler = liftSuspensionHandler;
        this.reportMapper = reportMapper;
    }

    /** The review queue, optionally filtered by status. */
    @GetMapping
    public ResponseEntity<ReportListResponse> queue(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        return ResponseEntity.ok(getReportQueueHandler.handle(
                new GetReportQueueQuery(status, page, size)));
    }

    /** Dashboard: pharmacies with the most unresolved reports. */
    @GetMapping("/flagged")
    public ResponseEntity<List<FlaggedPharmacyResponse>> flagged() {
        return ResponseEntity.ok(getFlaggedPharmaciesHandler.handle(new GetFlaggedPharmaciesQuery()));
    }

    /**
     * Moderation audit trail. Restricted to the super admin (ROLE_ADMIN) so it
     * serves as oversight of what report admins have done — a report admin
     * cannot view (or control) their own audit log.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/audit-logs")
    public ResponseEntity<AuditLogListResponse> auditLogs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        return ResponseEntity.ok(getAuditLogsHandler.handle(new GetAuditLogsQuery(page, size)));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ReportResponse> resolve(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable UUID id,
            @RequestBody(required = false) @Valid ResolveReportRequest request) {

        String note = request != null ? request.getNote() : null;
        return ResponseEntity.ok(reportMapper.toResponse(
                resolveReportHandler.handle(new ResolveReportCommand(id, parseUserId(userId), role, note))));
    }

    @PostMapping("/{id}/dismiss")
    public ResponseEntity<ReportResponse> dismiss(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable UUID id,
            @RequestBody(required = false) @Valid ResolveReportRequest request) {

        String note = request != null ? request.getNote() : null;
        return ResponseEntity.ok(reportMapper.toResponse(
                dismissReportHandler.handle(new DismissReportCommand(id, parseUserId(userId), role, note))));
    }

    @PostMapping("/pharmacies/{pharmacyId}/warn")
    public ResponseEntity<Map<String, String>> warn(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable UUID pharmacyId,
            @RequestBody(required = false) @Valid WarnPharmacyRequest request) {

        String reason = request != null ? request.getReason() : null;
        warnPharmacyHandler.handle(new WarnPharmacyCommand(pharmacyId, parseUserId(userId), role, reason));
        return ResponseEntity.ok(Map.of("status", "warned", "pharmacyId", pharmacyId.toString()));
    }

    @PostMapping("/pharmacies/{pharmacyId}/suspend")
    public ResponseEntity<Map<String, String>> suspend(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable UUID pharmacyId,
            @RequestBody(required = false) @Valid SuspendPharmacyRequest request) {

        Integer days = request != null ? request.getDays() : null;
        String reason = request != null ? request.getReason() : null;
        suspendPharmacyHandler.handle(
                new SuspendPharmacyCommand(pharmacyId, parseUserId(userId), role, days, reason));
        return ResponseEntity.ok(Map.of("status", "suspended", "pharmacyId", pharmacyId.toString()));
    }

    @PostMapping("/pharmacies/{pharmacyId}/lift-suspension")
    public ResponseEntity<Map<String, String>> liftSuspension(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable UUID pharmacyId) {

        liftSuspensionHandler.handle(new LiftSuspensionCommand(pharmacyId, parseUserId(userId), role));
        return ResponseEntity.ok(Map.of("status", "lifted", "pharmacyId", pharmacyId.toString()));
    }

    private UUID parseUserId(String userId) {
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid or missing X-User-Id header");
        }
    }
}
