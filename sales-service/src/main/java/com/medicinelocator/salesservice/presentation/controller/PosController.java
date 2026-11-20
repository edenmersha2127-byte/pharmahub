package com.medicinelocator.salesservice.presentation.controller;

import com.medicinelocator.salesservice.application.dto.request.CheckoutRequest;
import com.medicinelocator.salesservice.application.dto.response.ReceiptResponse;
import com.medicinelocator.salesservice.application.mapper.SaleMapper;
import com.medicinelocator.salesservice.application.service.CheckoutService;
import com.medicinelocator.salesservice.domain.model.Sale;
import com.medicinelocator.salesservice.infrastructure.security.CurrentUser;
import com.medicinelocator.salesservice.infrastructure.security.CurrentUserProvider;
import com.medicinelocator.salesservice.infrastructure.security.PermissionConstants;
import com.medicinelocator.salesservice.infrastructure.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Point-of-sale checkout. The pharmacy is implicit (the cashier's own), so no
 * pharmacyId is needed in the path. Requires SELL_MEDICINE.
 */
@RestController
@RequestMapping("/api/pos")
public class PosController {

    private final CheckoutService checkoutService;
    private final SaleMapper saleMapper;
    private final CurrentUserProvider currentUserProvider;
    private final SecurityUtils securityUtils;

    public PosController(CheckoutService checkoutService,
                         SaleMapper saleMapper,
                         CurrentUserProvider currentUserProvider,
                         SecurityUtils securityUtils) {
        this.checkoutService = checkoutService;
        this.saleMapper = saleMapper;
        this.currentUserProvider = currentUserProvider;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/checkout")
    public ResponseEntity<ReceiptResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        UUID pharmacyId = securityUtils.requirePharmacyContext(currentUser, PermissionConstants.SELL_MEDICINE);

        Sale sale = checkoutService.checkout(pharmacyId, currentUser.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saleMapper.toReceipt(sale));
    }
}
