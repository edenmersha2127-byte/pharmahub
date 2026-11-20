package com.medicinelocator.inventory.presentation.controller;

import com.medicinelocator.inventory.application.dto.request.SupplierRequest;
import com.medicinelocator.inventory.application.dto.response.MessageResponse;
import com.medicinelocator.inventory.application.dto.response.SupplierResponse;
import com.medicinelocator.inventory.application.mapper.SupplierMapper;
import com.medicinelocator.inventory.application.service.SubscriptionAccessChecker;
import com.medicinelocator.inventory.application.service.SupplierManagementService;
import com.medicinelocator.inventory.domain.exception.SubscriptionRequiredException;
import com.medicinelocator.inventory.domain.model.Supplier;
import com.medicinelocator.inventory.infrastructure.security.CurrentUser;
import com.medicinelocator.inventory.infrastructure.security.CurrentUserProvider;
import com.medicinelocator.inventory.infrastructure.security.PermissionConstants;
import com.medicinelocator.inventory.infrastructure.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Supplier CRUD for a pharmacy's own supplier book. All operations require the
 * MANAGE_SUPPLIERS capability (owners implicitly hold it); write operations are
 * additionally gated behind an active subscription, mirroring medicine changes.
 */
@RestController
@RequestMapping("/api/inventory/pharmacies/{pharmacyId}/suppliers")
public class SupplierController {

    private final SupplierManagementService supplierManagementService;
    private final SupplierMapper supplierMapper;
    private final CurrentUserProvider currentUserProvider;
    private final SecurityUtils securityUtils;
    private final SubscriptionAccessChecker subscriptionAccessChecker;

    public SupplierController(SupplierManagementService supplierManagementService,
                             SupplierMapper supplierMapper,
                             CurrentUserProvider currentUserProvider,
                             SecurityUtils securityUtils,
                             SubscriptionAccessChecker subscriptionAccessChecker) {
        this.supplierManagementService = supplierManagementService;
        this.supplierMapper = supplierMapper;
        this.currentUserProvider = currentUserProvider;
        this.securityUtils = securityUtils;
        this.subscriptionAccessChecker = subscriptionAccessChecker;
    }

    private void requireActiveSubscription(UUID pharmacyId) {
        if (!subscriptionAccessChecker.hasActiveSubscription(pharmacyId)) {
            throw new SubscriptionRequiredException(
                    "Your free trial or subscription has ended. Please subscribe to manage suppliers.");
        }
    }

    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(
            @PathVariable UUID pharmacyId,
            @Valid @RequestBody SupplierRequest request) {

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requirePharmacyAccess(currentUser, pharmacyId, PermissionConstants.MANAGE_SUPPLIERS);
        requireActiveSubscription(pharmacyId);

        Supplier supplier = supplierManagementService.create(pharmacyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierMapper.toResponse(supplier));
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> listSuppliers(@PathVariable UUID pharmacyId) {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requirePharmacyAccess(currentUser, pharmacyId, PermissionConstants.MANAGE_SUPPLIERS);

        List<SupplierResponse> suppliers = supplierManagementService.list(pharmacyId).stream()
                .map(supplierMapper::toResponse)
                .toList();
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/{supplierId}")
    public ResponseEntity<SupplierResponse> getSupplier(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID supplierId) {

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requirePharmacyAccess(currentUser, pharmacyId, PermissionConstants.MANAGE_SUPPLIERS);

        Supplier supplier = supplierManagementService.getById(pharmacyId, supplierId);
        return ResponseEntity.ok(supplierMapper.toResponse(supplier));
    }

    @PutMapping("/{supplierId}")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID supplierId,
            @Valid @RequestBody SupplierRequest request) {

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requirePharmacyAccess(currentUser, pharmacyId, PermissionConstants.MANAGE_SUPPLIERS);
        requireActiveSubscription(pharmacyId);

        Supplier supplier = supplierManagementService.update(pharmacyId, supplierId, request);
        return ResponseEntity.ok(supplierMapper.toResponse(supplier));
    }

    @DeleteMapping("/{supplierId}")
    public ResponseEntity<MessageResponse> deleteSupplier(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID supplierId) {

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requirePharmacyAccess(currentUser, pharmacyId, PermissionConstants.MANAGE_SUPPLIERS);
        requireActiveSubscription(pharmacyId);

        supplierManagementService.delete(pharmacyId, supplierId);
        return ResponseEntity.ok(new MessageResponse("Supplier deleted successfully"));
    }
}
