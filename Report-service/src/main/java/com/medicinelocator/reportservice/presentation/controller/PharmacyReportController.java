package com.medicinelocator.reportservice.presentation.controller;

import com.medicinelocator.reportservice.application.command.RespondToReportCommand;
import com.medicinelocator.reportservice.application.command.handler.RespondToReportHandler;
import com.medicinelocator.reportservice.application.dto.request.RespondToReportRequest;
import com.medicinelocator.reportservice.application.dto.response.ReportListResponse;
import com.medicinelocator.reportservice.application.dto.response.ReportResponse;
import com.medicinelocator.reportservice.application.mapper.ReportMapper;
import com.medicinelocator.reportservice.application.query.GetReportsForPharmacyQuery;
import com.medicinelocator.reportservice.application.query.handler.GetReportsForPharmacyHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Pharmacy-facing API: view reports filed against me and respond to them.
 * X-User-Id is the authenticated pharmacy's id.
 */
@RestController
@RequestMapping("/api/pharmacy/reports")
@Validated
public class PharmacyReportController {

    private final GetReportsForPharmacyHandler getReportsForPharmacyHandler;
    private final RespondToReportHandler respondToReportHandler;
    private final ReportMapper reportMapper;

    public PharmacyReportController(GetReportsForPharmacyHandler getReportsForPharmacyHandler,
                                    RespondToReportHandler respondToReportHandler,
                                    ReportMapper reportMapper) {
        this.getReportsForPharmacyHandler = getReportsForPharmacyHandler;
        this.respondToReportHandler = respondToReportHandler;
        this.reportMapper = reportMapper;
    }

    /** Reports filed against the current pharmacy. */
    @PreAuthorize("hasRole('PHARMACY')")
    @GetMapping
    public ResponseEntity<ReportListResponse> myReports(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        return ResponseEntity.ok(getReportsForPharmacyHandler.handle(
                new GetReportsForPharmacyQuery(parseUserId(userId), page, size)));
    }

    /** Respond to a report (right-to-respond). */
    @PreAuthorize("hasRole('PHARMACY')")
    @PostMapping("/{id}/respond")
    public ResponseEntity<ReportResponse> respond(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable UUID id,
            @Valid @RequestBody RespondToReportRequest request) {

        ReportResponse response = reportMapper.toResponse(
                respondToReportHandler.handle(new RespondToReportCommand(
                        id, parseUserId(userId), request.getResponse())));
        return ResponseEntity.ok(response);
    }

    private UUID parseUserId(String userId) {
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid or missing X-User-Id header");
        }
    }
}
