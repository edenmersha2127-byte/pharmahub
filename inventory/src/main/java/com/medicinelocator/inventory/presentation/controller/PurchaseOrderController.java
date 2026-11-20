package com.medicinelocator.inventory.presentation.controller;

import com.medicinelocator.inventory.application.dto.request.CreatePurchaseOrderRequest;
import com.medicinelocator.inventory.application.dto.response.PurchaseOrderResponse;
import com.medicinelocator.inventory.application.mapper.PurchaseOrderMapper;
import com.medicinelocator.inventory.application.service.PurchaseOrderService;
import com.medicinelocator.inventory.application.service.SubscriptionAccessChecker;
import com.medicinelocator.inventory.domain.exception.SubscriptionRequiredException;
import com.medicinelocator.inventory.domain.model.PurchaseOrder;
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
 * Purchase orders: raise a draft order (MANAGE_PURCHASES) and receive it into
 * stock (RECEIVE_STOCK). Owners implicitly hold both. Mutations require an
 * active subscription, consistent with the rest of inventory management.
 */
@RestController
@RequestMapping("/api/inventory/pharmacies/{pharmacyId}/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final CurrentUserProvider currentUserProvider;
    private final SecurityUtils securityUtils;
    private final SubscriptionAccessChecker subscriptionAccessChecker;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService,
                                  PurchaseOrderMapper purchaseOrderMapper,
                                  CurrentUserProvider currentUserProvider,
                                  SecurityUtils securityUtils,
                                  SubscriptionAccessChecker subscriptionAccessChecker) {
        this.purchaseOrderService = purchaseOrderService;
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.currentUserProvider = currentUserProvider;
        this.securityUtils = securityUtils;
        this.subscriptionAccessChecker = subscriptionAccessChecker;
    }

    private void requireActiveSubscription(UUID pharmacyId) {
        if (!subscriptionAccessChecker.hasActiveSubscription(pharmacyId)) {
            throw new SubscriptionRequiredException(
                    "Your free trial or subscription has ended. Please subscribe to manage purchases.");
        }
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(
            @PathVariable UUID pharmacyId,
            @Valid @RequestBody CreatePurchaseOrderRequest request) {

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requirePharmacyAccess(currentUser, pharmacyId, PermissionConstants.MANAGE_PURCHASES);
        requireActiveSubscription(pharmacyId);

        PurchaseOrder purchaseOrder = purchaseOrderService.create(pharmacyId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(purchaseOrderMapper.toResponse(purchaseOrder));
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponse>> listPurchaseOrders(@PathVariable UUID pharmacyId) {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requirePharmacyAccess(currentUser, pharmacyId, PermissionConstants.MANAGE_PURCHASES);

        List<PurchaseOrderResponse> orders = purchaseOrderService.list(pharmacyId).stream()
                .map(purchaseOrderMapper::toResponse)
                .toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{purchaseOrderId}")
    public ResponseEntity<PurchaseOrderResponse> getPurchaseOrder(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID purchaseOrderId) {

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requirePharmacyAccess(currentUser, pharmacyId, PermissionConstants.MANAGE_PURCHASES);

        PurchaseOrder purchaseOrder = purchaseOrderService.getById(pharmacyId, purchaseOrderId);
        return ResponseEntity.ok(purchaseOrderMapper.toResponse(purchaseOrder));
    }

    @PostMapping("/{purchaseOrderId}/receive")
    public ResponseEntity<PurchaseOrderResponse> receivePurchaseOrder(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID purchaseOrderId) {

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requirePharmacyAccess(currentUser, pharmacyId, PermissionConstants.RECEIVE_STOCK);
        requireActiveSubscription(pharmacyId);

        PurchaseOrder purchaseOrder = purchaseOrderService.receive(pharmacyId, purchaseOrderId);
        return ResponseEntity.ok(purchaseOrderMapper.toResponse(purchaseOrder));
    }
}
