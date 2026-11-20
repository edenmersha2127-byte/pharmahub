package com.medicinelocator.salesservice.presentation.controller;

import com.medicinelocator.salesservice.application.dto.response.DailySummaryResponse;
import com.medicinelocator.salesservice.application.dto.response.ReceiptResponse;
import com.medicinelocator.salesservice.application.dto.response.SaleSummaryResponse;
import com.medicinelocator.salesservice.application.dto.response.SalesAnalyticsResponse;
import com.medicinelocator.salesservice.application.dto.response.SalesTimeseriesResponse;
import com.medicinelocator.salesservice.application.dto.response.TopMedicineResponse;
import com.medicinelocator.salesservice.application.mapper.SaleMapper;
import com.medicinelocator.salesservice.application.service.SalesQueryService;
import com.medicinelocator.salesservice.domain.enums.Granularity;
import com.medicinelocator.salesservice.domain.model.Sale;
import com.medicinelocator.salesservice.infrastructure.security.CurrentUser;
import com.medicinelocator.salesservice.infrastructure.security.CurrentUserProvider;
import com.medicinelocator.salesservice.infrastructure.security.PermissionConstants;
import com.medicinelocator.salesservice.infrastructure.security.SecurityUtils;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Sales history and reporting. Listing/summary require VIEW_REPORTS; fetching a
 * single receipt requires PRINT_RECEIPT.
 */
@RestController
@RequestMapping("/api/sales")
@Validated
public class SalesController {

    private final SalesQueryService salesQueryService;
    private final SaleMapper saleMapper;
    private final CurrentUserProvider currentUserProvider;
    private final SecurityUtils securityUtils;

    public SalesController(SalesQueryService salesQueryService,
                          SaleMapper saleMapper,
                          CurrentUserProvider currentUserProvider,
                          SecurityUtils securityUtils) {
        this.salesQueryService = salesQueryService;
        this.saleMapper = saleMapper;
        this.currentUserProvider = currentUserProvider;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public ResponseEntity<List<SaleSummaryResponse>> listSales() {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        UUID pharmacyId = securityUtils.requirePharmacyContext(currentUser, PermissionConstants.VIEW_REPORTS);

        List<SaleSummaryResponse> sales = salesQueryService.listSales(pharmacyId).stream()
                .map(saleMapper::toSummary)
                .toList();
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/summary/today")
    public ResponseEntity<DailySummaryResponse> todaySummary() {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        UUID pharmacyId = securityUtils.requirePharmacyContext(currentUser, PermissionConstants.VIEW_REPORTS);
        return ResponseEntity.ok(salesQueryService.todaySummary(pharmacyId));
    }

    @GetMapping("/analytics")
    public ResponseEntity<SalesAnalyticsResponse> analytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        UUID pharmacyId = securityUtils.requirePharmacyContext(currentUser, PermissionConstants.VIEW_REPORTS);

        LocalDate today = LocalDate.now();
        LocalDate effectiveTo = to != null ? to : today;
        LocalDate effectiveFrom = from != null ? from : today.withDayOfMonth(1);
        return ResponseEntity.ok(salesQueryService.analytics(pharmacyId, effectiveFrom, effectiveTo));
    }

    @GetMapping("/analytics/top-medicines")
    public ResponseEntity<List<TopMedicineResponse>> topMedicines(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        UUID pharmacyId = securityUtils.requirePharmacyContext(currentUser, PermissionConstants.VIEW_REPORTS);

        LocalDate today = LocalDate.now();
        LocalDate effectiveTo = to != null ? to : today;
        LocalDate effectiveFrom = from != null ? from : today.withDayOfMonth(1);
        return ResponseEntity.ok(salesQueryService.topMedicines(pharmacyId, effectiveFrom, effectiveTo, limit));
    }

    @GetMapping("/analytics/timeseries")
    public ResponseEntity<List<SalesTimeseriesResponse>> timeseries(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "DAY") Granularity granularity) {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        UUID pharmacyId = securityUtils.requirePharmacyContext(currentUser, PermissionConstants.VIEW_REPORTS);

        LocalDate today = LocalDate.now();
        LocalDate effectiveTo = to != null ? to : today;
        LocalDate effectiveFrom = from != null ? from : today.withDayOfMonth(1);
        return ResponseEntity.ok(
                salesQueryService.timeseries(pharmacyId, effectiveFrom, effectiveTo, granularity));
    }

    @GetMapping("/{saleId}")
    public ResponseEntity<ReceiptResponse> getSale(@PathVariable UUID saleId) {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        UUID pharmacyId = securityUtils.requirePharmacyContext(currentUser, PermissionConstants.PRINT_RECEIPT);

        Sale sale = salesQueryService.getSale(pharmacyId, saleId);
        return ResponseEntity.ok(saleMapper.toReceipt(sale));
    }
}
