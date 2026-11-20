package com.medicinelocator.salesservice.presentation.controller;

import com.medicinelocator.salesservice.application.dto.request.PharmacyCustomerRequest;
import com.medicinelocator.salesservice.application.dto.response.CustomerDetailResponse;
import com.medicinelocator.salesservice.application.dto.response.CustomerResponse;
import com.medicinelocator.salesservice.application.dto.response.MessageResponse;
import com.medicinelocator.salesservice.application.dto.response.SaleSummaryResponse;
import com.medicinelocator.salesservice.application.mapper.PharmacyCustomerMapper;
import com.medicinelocator.salesservice.application.mapper.SaleMapper;
import com.medicinelocator.salesservice.application.service.PharmacyCustomerService;
import com.medicinelocator.salesservice.application.service.SalesQueryService;
import com.medicinelocator.salesservice.domain.model.PharmacyCustomer;
import com.medicinelocator.salesservice.infrastructure.security.CurrentUser;
import com.medicinelocator.salesservice.infrastructure.security.CurrentUserProvider;
import com.medicinelocator.salesservice.infrastructure.security.PermissionConstants;
import com.medicinelocator.salesservice.infrastructure.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Walk-in customer CRM. All operations require MANAGE_CUSTOMERS (owner implicit).
 */
@RestController
@RequestMapping("/api/pharmacy-customers")
public class PharmacyCustomerController {

    private final PharmacyCustomerService customerService;
    private final SalesQueryService salesQueryService;
    private final PharmacyCustomerMapper customerMapper;
    private final SaleMapper saleMapper;
    private final CurrentUserProvider currentUserProvider;
    private final SecurityUtils securityUtils;

    public PharmacyCustomerController(PharmacyCustomerService customerService,
                                      SalesQueryService salesQueryService,
                                      PharmacyCustomerMapper customerMapper,
                                      SaleMapper saleMapper,
                                      CurrentUserProvider currentUserProvider,
                                      SecurityUtils securityUtils) {
        this.customerService = customerService;
        this.salesQueryService = salesQueryService;
        this.customerMapper = customerMapper;
        this.saleMapper = saleMapper;
        this.currentUserProvider = currentUserProvider;
        this.securityUtils = securityUtils;
    }

    private UUID authorize() {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        return securityUtils.requirePharmacyContext(currentUser, PermissionConstants.MANAGE_CUSTOMERS);
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody PharmacyCustomerRequest request) {
        UUID pharmacyId = authorize();
        PharmacyCustomer customer = customerService.create(pharmacyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(customerMapper.toResponse(customer));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> list() {
        UUID pharmacyId = authorize();
        List<CustomerResponse> customers = customerService.list(pharmacyId).stream()
                .map(customerMapper::toResponse)
                .toList();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerDetailResponse> getWithHistory(@PathVariable UUID customerId) {
        UUID pharmacyId = authorize();
        PharmacyCustomer customer = customerService.getById(pharmacyId, customerId);
        List<SaleSummaryResponse> history = salesQueryService.customerPurchases(pharmacyId, customerId).stream()
                .map(saleMapper::toSummary)
                .toList();
        return ResponseEntity.ok(new CustomerDetailResponse(customerMapper.toResponse(customer), history));
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> update(@PathVariable UUID customerId,
                                                   @Valid @RequestBody PharmacyCustomerRequest request) {
        UUID pharmacyId = authorize();
        PharmacyCustomer customer = customerService.update(pharmacyId, customerId, request);
        return ResponseEntity.ok(customerMapper.toResponse(customer));
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<MessageResponse> delete(@PathVariable UUID customerId) {
        UUID pharmacyId = authorize();
        customerService.delete(pharmacyId, customerId);
        return ResponseEntity.ok(new MessageResponse("Customer deleted successfully"));
    }
}
