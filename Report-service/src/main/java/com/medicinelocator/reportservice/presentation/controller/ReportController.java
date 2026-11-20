package com.medicinelocator.reportservice.presentation.controller;

import com.medicinelocator.reportservice.application.command.SubmitReportCommand;
import com.medicinelocator.reportservice.application.command.handler.SubmitReportHandler;
import com.medicinelocator.reportservice.application.dto.request.SubmitReportRequest;
import com.medicinelocator.reportservice.application.dto.response.ReportListResponse;
import com.medicinelocator.reportservice.application.dto.response.ReportResponse;
import com.medicinelocator.reportservice.application.mapper.ReportMapper;
import com.medicinelocator.reportservice.application.query.GetMyReportsQuery;
import com.medicinelocator.reportservice.application.query.handler.GetMyReportsHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Customer-facing reporting API (behind the gateway's JwtAuth filter, which
 * injects X-User-Id identifying the reporting customer).
 */
@RestController
@RequestMapping("/api/reports")
@Validated
public class ReportController {

    private final SubmitReportHandler submitReportHandler;
    private final GetMyReportsHandler getMyReportsHandler;
    private final ReportMapper reportMapper;

    public ReportController(SubmitReportHandler submitReportHandler,
                            GetMyReportsHandler getMyReportsHandler,
                            ReportMapper reportMapper) {
        this.submitReportHandler = submitReportHandler;
        this.getMyReportsHandler = getMyReportsHandler;
        this.reportMapper = reportMapper;
    }

    /** File a report against a pharmacy. */
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping
    public ResponseEntity<ReportResponse> submitReport(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody SubmitReportRequest request) {

        ReportResponse response = reportMapper.toResponse(
                submitReportHandler.handle(new SubmitReportCommand(
                        parseUserId(userId),
                        request.getPharmacyId(),
                        request.getCategory(),
                        request.getDescription())));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** List the reports the current customer has filed. */
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/mine")
    public ResponseEntity<ReportListResponse> myReports(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        return ResponseEntity.ok(getMyReportsHandler.handle(
                new GetMyReportsQuery(parseUserId(userId), page, size)));
    }

    private UUID parseUserId(String userId) {
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid or missing X-User-Id header");
        }
    }
}
