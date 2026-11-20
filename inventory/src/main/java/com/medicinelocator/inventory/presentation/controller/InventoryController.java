package com.medicinelocator.inventory.presentation.controller;

import com.medicinelocator.inventory.application.command.*;
import com.medicinelocator.inventory.application.command.handler.*;
import com.medicinelocator.inventory.application.dto.request.AddMedicineRequest;
import com.medicinelocator.inventory.application.dto.request.DeductBatchRequest;
import com.medicinelocator.inventory.application.dto.request.UpdateStockRequest;
import com.medicinelocator.inventory.application.dto.response.*;
import com.medicinelocator.inventory.application.mapper.MedicineMapper;
import com.medicinelocator.inventory.application.query.*;
import com.medicinelocator.inventory.application.query.handler.*;
import com.medicinelocator.inventory.application.service.MedicineService;
import com.medicinelocator.inventory.application.service.SubscriptionAccessChecker;
import com.medicinelocator.inventory.domain.exception.MedicineNotFoundException;
import com.medicinelocator.inventory.domain.exception.SubscriptionRequiredException;
import com.medicinelocator.inventory.domain.model.Medicine;
import com.medicinelocator.inventory.infrastructure.security.CurrentUser;
import com.medicinelocator.inventory.infrastructure.security.CurrentUserProvider;
import com.medicinelocator.inventory.infrastructure.security.PermissionConstants;
import com.medicinelocator.inventory.infrastructure.security.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/inventory")
@Validated
public class InventoryController {

    private final AddMedicineHandler addMedicineHandler;
    private final UpdateStockHandler updateStockHandler;
    private final DeductStockHandler deductStockHandler;
    private final BatchDeductHandler batchDeductHandler;
    private final RemoveMedicineHandler removeMedicineHandler;
    private final GetPharmacyInventoryHandler getPharmacyInventoryHandler;
    private final GetMedicineByIdHandler getMedicineByIdHandler;
    private final SearchMedicinesHandler searchMedicinesHandler;
    private final MedicineMapper medicineMapper;
    private final MedicineService medicineService;
    private final CurrentUserProvider currentUserProvider;
    private final SecurityUtils securityUtils;
    private final SubscriptionAccessChecker subscriptionAccessChecker;

    public InventoryController(AddMedicineHandler addMedicineHandler,
                               UpdateStockHandler updateStockHandler,
                               DeductStockHandler deductStockHandler,
                               BatchDeductHandler batchDeductHandler,
                               RemoveMedicineHandler removeMedicineHandler,
                               GetPharmacyInventoryHandler getPharmacyInventoryHandler,
                               GetMedicineByIdHandler getMedicineByIdHandler,
                               SearchMedicinesHandler searchMedicinesHandler,
                               MedicineMapper medicineMapper,
                               MedicineService medicineService,
                               CurrentUserProvider currentUserProvider,
                               SecurityUtils securityUtils,
                               SubscriptionAccessChecker subscriptionAccessChecker) {
        this.addMedicineHandler = addMedicineHandler;
        this.updateStockHandler = updateStockHandler;
        this.deductStockHandler = deductStockHandler;
        this.batchDeductHandler = batchDeductHandler;
        this.removeMedicineHandler = removeMedicineHandler;
        this.getPharmacyInventoryHandler = getPharmacyInventoryHandler;
        this.getMedicineByIdHandler = getMedicineByIdHandler;
        this.searchMedicinesHandler = searchMedicinesHandler;
        this.medicineMapper = medicineMapper;
        this.medicineService = medicineService;
        this.currentUserProvider = currentUserProvider;
        this.securityUtils = securityUtils;
        this.subscriptionAccessChecker = subscriptionAccessChecker;
    }

    /**
     * Subscription paywall: a pharmacy without an active subscription/trial may
     * not modify its inventory (add/update/deduct/remove). Read-only endpoints
     * stay open so it can still see its catalogue and log in to subscribe.
     */
    private void requireActiveSubscription(UUID pharmacyId) {
        if (!subscriptionAccessChecker.hasActiveSubscription(pharmacyId)) {
            throw new SubscriptionRequiredException(
                    "Your free trial or subscription has ended. Please subscribe to manage inventory, "
                            + "use the system, and appear in customer search.");
        }
    }


    @PostMapping("/pharmacies/{pharmacyId}/medicines")
    public ResponseEntity<MedicineResponse> addMedicine(
            @PathVariable UUID pharmacyId,
            @Valid @RequestBody AddMedicineRequest request) {

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requirePharmacyAccess(currentUser, pharmacyId, PermissionConstants.MANAGE_MEDICINES);
        requireActiveSubscription(pharmacyId);

        AddMedicineCommand command = medicineMapper.toAddMedicineCommand(pharmacyId, request);
        Medicine medicine = addMedicineHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(medicineMapper.toMedicineResponse(medicine));
    }

    @PutMapping("/pharmacies/{pharmacyId}/medicines/{medicineId}")
    public ResponseEntity<MedicineResponse> updateMedicine(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID medicineId,
            @Valid @RequestBody UpdateStockRequest request) {

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requirePharmacyAccess(currentUser, pharmacyId, PermissionConstants.MANAGE_MEDICINES);
        requireActiveSubscription(pharmacyId);

        UpdateStockCommand command = medicineMapper.toUpdateStockCommand(pharmacyId, medicineId, request);
        Medicine medicine = updateStockHandler.handle(command);
        return ResponseEntity.ok(medicineMapper.toMedicineResponse(medicine));
    }


    @DeleteMapping("/pharmacies/{pharmacyId}/medicines/{medicineId}")
    public ResponseEntity<MessageResponse> removeMedicine(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID medicineId) {

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requirePharmacyAccess(currentUser, pharmacyId, PermissionConstants.MANAGE_MEDICINES);
        requireActiveSubscription(pharmacyId);

        removeMedicineHandler.handle(new RemoveMedicineCommand(pharmacyId, medicineId));
        return ResponseEntity.ok(new MessageResponse("Medicine removed successfully"));
    }


    @PostMapping("/pharmacies/{pharmacyId}/medicines/{medicineId}/deduct")
    public ResponseEntity<MedicineResponse> deductStock(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID medicineId,
            @RequestParam @Positive(message = "Amount must be positive") int amount) {

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requirePharmacyAccess(currentUser, pharmacyId, PermissionConstants.SELL_MEDICINE);
        requireActiveSubscription(pharmacyId);

        DeductStockCommand command = new DeductStockCommand(pharmacyId, medicineId, amount);
        Medicine medicine = deductStockHandler.handle(command);
        return ResponseEntity.ok(medicineMapper.toMedicineResponse(medicine));
    }


    /**
     * Atomic multi-line deduction for POS checkout (called by the sales-service).
     * Deducts the whole cart in one transaction; any failing line rolls back all.
     */
    @PostMapping("/pharmacies/{pharmacyId}/medicines/deduct-batch")
    public ResponseEntity<List<MedicineResponse>> deductBatch(
            @PathVariable UUID pharmacyId,
            @Valid @RequestBody DeductBatchRequest request) {

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requirePharmacyAccess(currentUser, pharmacyId, PermissionConstants.SELL_MEDICINE);
        requireActiveSubscription(pharmacyId);

        List<MedicineResponse> deducted = batchDeductHandler.handle(pharmacyId, request).stream()
                .map(medicineMapper::toMedicineResponse)
                .toList();
        return ResponseEntity.ok(deducted);
    }


    @GetMapping("/pharmacies/{pharmacyId}/medicines")
    public ResponseEntity<PharmacyInventoryResponse> getPharmacyInventory(
            @PathVariable UUID pharmacyId,
            @RequestParam(defaultValue = "false") boolean availableOnly,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requirePharmacyAccess(currentUser, pharmacyId, PermissionConstants.VIEW_INVENTORY);

        GetPharmacyInventoryQuery query = new GetPharmacyInventoryQuery(
                pharmacyId, availableOnly, page, size);
        return ResponseEntity.ok(getPharmacyInventoryHandler.handle(query));
    }


    /** Barcode (GTIN/EAN) scan lookup for POS / receiving. */
    @GetMapping("/pharmacies/{pharmacyId}/medicines/barcode/{barcode}")
    public ResponseEntity<MedicineResponse> getMedicineByBarcode(
            @PathVariable UUID pharmacyId,
            @PathVariable String barcode) {

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requirePharmacyAccess(currentUser, pharmacyId, PermissionConstants.VIEW_INVENTORY);

        Medicine medicine = medicineService.findByBarcode(pharmacyId, barcode)
                .orElseThrow(() -> new MedicineNotFoundException(
                        "No medicine found with barcode '" + barcode + "' for pharmacy: " + pharmacyId));
        return ResponseEntity.ok(medicineMapper.toMedicineResponse(medicine));
    }

    /**
     * Pharmacy-scoped name lookup: resolves medicine names (e.g. from a
     * prescription's OCR output, or a POS type-ahead) to this pharmacy's own
     * medicines so they can be added to a POS cart.
     */
    @GetMapping("/pharmacies/{pharmacyId}/medicines/lookup")
    public ResponseEntity<List<MedicineResponse>> lookupMedicines(
            @PathVariable UUID pharmacyId,
            @RequestParam("q") String q) {

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requirePharmacyAccess(currentUser, pharmacyId, PermissionConstants.VIEW_INVENTORY);

        List<MedicineResponse> matches = medicineService.searchByName(pharmacyId, q).stream()
                .map(medicineMapper::toMedicineResponse)
                .toList();
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/pharmacies/{pharmacyId}/medicines/{medicineId}")
    public ResponseEntity<MedicineResponse> getMedicineById(
            @PathVariable UUID pharmacyId,
            @PathVariable UUID medicineId) {

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requirePharmacyAccess(currentUser, pharmacyId, PermissionConstants.VIEW_INVENTORY);

        GetMedicineByIdQuery query = new GetMedicineByIdQuery(pharmacyId, medicineId);
        return ResponseEntity.ok(getMedicineByIdHandler.handle(query));
    }



    @GetMapping("/medicines/search")
    public ResponseEntity<PagedResponse<MedicineSearchResponse>> searchMedicines(
            @RequestParam(required = false) String medicineName,
            @RequestParam(required = false) String brandName,
            @RequestParam(required = false) String genericName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean availableOnly,
            @RequestParam(required = false) Boolean requiresPrescription,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        securityUtils.requireAuthenticated(currentUser);

        SearchMedicinesQuery query = new SearchMedicinesQuery(
                medicineName, brandName, genericName, category,
                availableOnly, requiresPrescription, page, size);

        return ResponseEntity.ok(searchMedicinesHandler.handle(query));
    }
}