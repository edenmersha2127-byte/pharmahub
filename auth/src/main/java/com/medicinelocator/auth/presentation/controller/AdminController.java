package com.medicinelocator.auth.presentation.controller;

import com.medicinelocator.auth.application.command.ApprovePharmacyCommand;
import com.medicinelocator.auth.application.command.CreateReportAdminCommand;
import com.medicinelocator.auth.application.command.RejectPharmacyCommand;
import com.medicinelocator.auth.application.command.handler.ApprovePharmacyHandler;
import com.medicinelocator.auth.application.command.handler.CreateReportAdminHandler;
import com.medicinelocator.auth.application.command.handler.RejectPharmacyHandler;
import com.medicinelocator.auth.application.dto.request.CreateReportAdminRequest;
import com.medicinelocator.auth.application.dto.response.AdminAccountResponse;
import com.medicinelocator.auth.application.dto.response.PagedResponse;
import com.medicinelocator.auth.application.dto.response.PharmacyAdminResponse;
import com.medicinelocator.auth.application.dto.response.UserAdminResponse;
import com.medicinelocator.auth.application.mapper.AdminMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import com.medicinelocator.auth.application.query.GetAllUsersQuery;
import com.medicinelocator.auth.application.query.GetPharmaciesQuery;
import com.medicinelocator.auth.application.query.GetPharmacyByIdQuery;
import com.medicinelocator.auth.application.query.handler.GetAllUsersHandler;
import com.medicinelocator.auth.application.query.handler.GetPharmaciesHandler;
import com.medicinelocator.auth.application.query.handler.GetPharmacyByIdHandler;
import com.medicinelocator.auth.domain.enums.PharmacyStatus;
import com.medicinelocator.auth.domain.model.Pharmacy;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/admin")
@Validated
public class AdminController {

    private final GetPharmaciesHandler getPharmaciesHandler;
    private final GetPharmacyByIdHandler getPharmacyByIdHandler;
    private final ApprovePharmacyHandler approvePharmacyHandler;
    private final RejectPharmacyHandler rejectPharmacyHandler;
    private final GetAllUsersHandler getAllUsersHandler;
    private final CreateReportAdminHandler createReportAdminHandler;
    private final AdminMapper adminMapper;

    public AdminController(GetPharmaciesHandler getPharmaciesHandler,
                           GetPharmacyByIdHandler getPharmacyByIdHandler,
                           ApprovePharmacyHandler approvePharmacyHandler,
                           RejectPharmacyHandler rejectPharmacyHandler,
                           GetAllUsersHandler getAllUsersHandler,
                           CreateReportAdminHandler createReportAdminHandler,
                           AdminMapper adminMapper) {
        this.getPharmaciesHandler = getPharmaciesHandler;
        this.getPharmacyByIdHandler = getPharmacyByIdHandler;
        this.approvePharmacyHandler = approvePharmacyHandler;
        this.rejectPharmacyHandler = rejectPharmacyHandler;
        this.getAllUsersHandler = getAllUsersHandler;
        this.createReportAdminHandler = createReportAdminHandler;
        this.adminMapper = adminMapper;
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pharmacies")
    public ResponseEntity<PagedResponse<PharmacyAdminResponse>> getPharmacies(
            @RequestParam(required = false) PharmacyStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        GetPharmaciesQuery query = new GetPharmaciesQuery(status, page, size);
        return ResponseEntity.ok(getPharmaciesHandler.handle(query));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pharmacies/{id}")
    public ResponseEntity<PharmacyAdminResponse> getPharmacyById(@PathVariable UUID id) {
        GetPharmacyByIdQuery query = new GetPharmacyByIdQuery(id);
        return ResponseEntity.ok(getPharmacyByIdHandler.handle(query));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/pharmacies/{id}/approve")
    public ResponseEntity<PharmacyAdminResponse> approvePharmacy(@PathVariable UUID id) {
        Pharmacy pharmacy = approvePharmacyHandler.handle(new ApprovePharmacyCommand(id));
        return ResponseEntity.ok(adminMapper.toPharmacyAdminResponse(pharmacy));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/pharmacies/{id}/reject")
    public ResponseEntity<PharmacyAdminResponse> rejectPharmacy(@PathVariable UUID id) {
        Pharmacy pharmacy = rejectPharmacyHandler.handle(new RejectPharmacyCommand(id));
        return ResponseEntity.ok(adminMapper.toPharmacyAdminResponse(pharmacy));
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<PagedResponse<UserAdminResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        GetAllUsersQuery query = new GetAllUsersQuery(page, size);
        return ResponseEntity.ok(getAllUsersHandler.handle(query));
    }

    /**
     * Create a REPORT_ADMIN sub-admin. Only the super admin (ROLE_ADMIN) may
     * do this — sub-admins cannot create other admins.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/report-admins")
    public ResponseEntity<AdminAccountResponse> createReportAdmin(
            @Valid @RequestBody CreateReportAdminRequest request) {

        AdminAccountResponse response = AdminAccountResponse.from(
                createReportAdminHandler.handle(new CreateReportAdminCommand(
                        request.getEmail(),
                        request.getPassword(),
                        request.getFirstName(),
                        request.getLastName())));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}