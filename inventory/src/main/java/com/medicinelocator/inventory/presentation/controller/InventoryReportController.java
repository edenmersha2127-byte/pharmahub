package com.medicinelocator.inventory.presentation.controller;

import com.medicinelocator.inventory.application.dto.response.InventoryStatsResponse;
import com.medicinelocator.inventory.application.dto.response.InventoryTrendsResponse;
import com.medicinelocator.inventory.application.dto.response.MedicineBatchResponse;
import com.medicinelocator.inventory.application.dto.response.MedicineResponse;
import com.medicinelocator.inventory.application.dto.response.StockMovementResponse;
import com.medicinelocator.inventory.application.dto.response.StockMovementSummaryResponse;
import com.medicinelocator.inventory.application.mapper.MedicineBatchMapper;
import com.medicinelocator.inventory.application.mapper.MedicineMapper;
import com.medicinelocator.inventory.application.mapper.StockMovementMapper;
import com.medicinelocator.inventory.application.service.InventoryReportService;
import com.medicinelocator.inventory.infrastructure.security.CurrentUser;
import com.medicinelocator.inventory.infrastructure.security.CurrentUserProvider;
import com.medicinelocator.inventory.infrastructure.security.PermissionConstants;
import com.medicinelocator.inventory.infrastructure.security.SecurityUtils;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Read-only inventory reporting endpoints (stock-movement history, batches,
 * low-stock, expiring/expired). All require VIEW_INVENTORY (owners implicit).
 */
@RestController
@RequestMapping("/api/inventory/pharmacies/{pharmacyId}")
@Validated
public class InventoryReportController {

    private final InventoryReportService reportService;
    private final MedicineMapper medicineMapper;
    private final MedicineBatchMapper batchMapper;
    private final StockMovementMapper stockMovementMapper;
    private final CurrentUserProvider currentUserProvider;
    private final SecurityUtils securityUtils;

    public InventoryReportController(InventoryReportService reportService,
                                     MedicineMapper medicineMapper,
                                     MedicineBatchMapper batchMapper,
                                     StockMovementMapper stockMovementMapper,
                                     CurrentUserProvider currentUserProvider,
                                     SecurityUtils securityUtils) {
        this.reportService = reportService;
        this.medicineMapper = medicineMapper;
        this.batchMapper = batchMapper;
        this.stockMovementMapper = stockMovementMapper;
        this.currentUserProvider = currentUserProvider;
        this.securityUtils = securityUtils;
    }

    private void authorize(UUID pharmacyId) {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requirePharmacyAccess(currentUser, pharmacyId, PermissionConstants.VIEW_INVENTORY);
    }

    @GetMapping("/medicines/{medicineId}/movements")
    public ResponseEntity<List<StockMovementResponse>> stockMovements(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID medicineId) {
        authorize(pharmacyId);
        List<StockMovementResponse> movements = reportService.stockMovements(pharmacyId, medicineId).stream()
                .map(stockMovementMapper::toResponse)
                .toList();
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/medicines/{medicineId}/batches")
    public ResponseEntity<List<MedicineBatchResponse>> batches(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID medicineId) {
        authorize(pharmacyId);
        List<MedicineBatchResponse> batches = reportService.batches(medicineId).stream()
                .map(batchMapper::toResponse)
                .toList();
        return ResponseEntity.ok(batches);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<MedicineResponse>> lowStock(@PathVariable UUID pharmacyId) {
        authorize(pharmacyId);
        List<MedicineResponse> medicines = reportService.lowStock(pharmacyId).stream()
                .map(medicineMapper::toMedicineResponse)
                .toList();
        return ResponseEntity.ok(medicines);
    }

    @GetMapping("/stats")
    public ResponseEntity<InventoryStatsResponse> stats(@PathVariable UUID pharmacyId) {
        authorize(pharmacyId);
        return ResponseEntity.ok(reportService.stats(pharmacyId));
    }

    @GetMapping("/stats/trends")
    public ResponseEntity<InventoryTrendsResponse> trends(@PathVariable UUID pharmacyId) {
        authorize(pharmacyId);
        return ResponseEntity.ok(reportService.trends(pharmacyId));
    }

    @GetMapping("/movements/summary")
    public ResponseEntity<List<StockMovementSummaryResponse>> movementsSummary(
            @PathVariable UUID pharmacyId,
            @RequestParam(defaultValue = "7") @Min(1) @Max(24) int months) {
        authorize(pharmacyId);
        return ResponseEntity.ok(reportService.movementsSummary(pharmacyId, months));
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<MedicineBatchResponse>> expiringSoon(
            @PathVariable UUID pharmacyId,
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int days) {
        authorize(pharmacyId);
        List<MedicineBatchResponse> batches = reportService.expiringSoon(pharmacyId, days).stream()
                .map(batchMapper::toResponse)
                .toList();
        return ResponseEntity.ok(batches);
    }

    @GetMapping("/expired")
    public ResponseEntity<List<MedicineBatchResponse>> expired(@PathVariable UUID pharmacyId) {
        authorize(pharmacyId);
        List<MedicineBatchResponse> batches = reportService.expired(pharmacyId).stream()
                .map(batchMapper::toResponse)
                .toList();
        return ResponseEntity.ok(batches);
    }
}
