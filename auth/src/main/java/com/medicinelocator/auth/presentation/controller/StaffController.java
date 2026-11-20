package com.medicinelocator.auth.presentation.controller;

import com.medicinelocator.auth.application.command.CreateStaffCommand;
import com.medicinelocator.auth.application.command.SetStaffActiveCommand;
import com.medicinelocator.auth.application.command.UpdateStaffPermissionsCommand;
import com.medicinelocator.auth.application.command.handler.CreateStaffHandler;
import com.medicinelocator.auth.application.command.handler.SetStaffActiveHandler;
import com.medicinelocator.auth.application.command.handler.UpdateStaffPermissionsHandler;
import com.medicinelocator.auth.application.dto.request.CreateStaffRequest;
import com.medicinelocator.auth.application.dto.request.UpdateStaffPermissionsRequest;
import com.medicinelocator.auth.application.dto.response.MessageResponse;
import com.medicinelocator.auth.application.dto.response.StaffResponse;
import com.medicinelocator.auth.application.mapper.StaffMapper;
import com.medicinelocator.auth.application.query.GetStaffByPharmacyQuery;
import com.medicinelocator.auth.application.query.handler.GetStaffByPharmacyHandler;
import com.medicinelocator.auth.domain.enums.Permission;
import com.medicinelocator.auth.infrastructure.security.GatewayAuthenticatedPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Pharmacy-owner-only management of that pharmacy's staff. The owner's own
 * userId IS the pharmacyId, so every action is scoped to the caller — an owner
 * can never touch another pharmacy's staff.
 */
@RestController
@RequestMapping("/api/pharmacy/staff")
public class StaffController {

    private final CreateStaffHandler createStaffHandler;
    private final UpdateStaffPermissionsHandler updateStaffPermissionsHandler;
    private final SetStaffActiveHandler setStaffActiveHandler;
    private final GetStaffByPharmacyHandler getStaffByPharmacyHandler;
    private final StaffMapper staffMapper;

    public StaffController(CreateStaffHandler createStaffHandler,
                           UpdateStaffPermissionsHandler updateStaffPermissionsHandler,
                           SetStaffActiveHandler setStaffActiveHandler,
                           GetStaffByPharmacyHandler getStaffByPharmacyHandler,
                           StaffMapper staffMapper) {
        this.createStaffHandler = createStaffHandler;
        this.updateStaffPermissionsHandler = updateStaffPermissionsHandler;
        this.setStaffActiveHandler = setStaffActiveHandler;
        this.getStaffByPharmacyHandler = getStaffByPharmacyHandler;
        this.staffMapper = staffMapper;
    }

    /** The menu of permission boxes an owner can tick. Used by the UI. */
    @PreAuthorize("hasRole('PHARMACY')")
    @GetMapping("/permissions")
    public ResponseEntity<Permission[]> getAvailablePermissions() {
        return ResponseEntity.ok(Permission.values());
    }

    @PreAuthorize("hasRole('PHARMACY')")
    @PostMapping
    public ResponseEntity<StaffResponse> createStaff(
            @AuthenticationPrincipal GatewayAuthenticatedPrincipal principal,
            @Valid @RequestBody CreateStaffRequest request) {

        CreateStaffCommand command = new CreateStaffCommand(
                principal.getUserId(),
                request.getEmail(),
                request.getPassword(),
                request.getFullName(),
                request.getJobTitle(),
                request.getPermissions());

        StaffResponse response = staffMapper.toResponse(createStaffHandler.handle(command));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('PHARMACY')")
    @GetMapping
    public ResponseEntity<List<StaffResponse>> listStaff(
            @AuthenticationPrincipal GatewayAuthenticatedPrincipal principal) {

        return ResponseEntity.ok(
                getStaffByPharmacyHandler.handle(new GetStaffByPharmacyQuery(principal.getUserId())));
    }

    @PreAuthorize("hasRole('PHARMACY')")
    @PutMapping("/{staffId}")
    public ResponseEntity<StaffResponse> updateStaff(
            @AuthenticationPrincipal GatewayAuthenticatedPrincipal principal,
            @PathVariable UUID staffId,
            @Valid @RequestBody UpdateStaffPermissionsRequest request) {

        UpdateStaffPermissionsCommand command = new UpdateStaffPermissionsCommand(
                principal.getUserId(),
                staffId,
                request.getFullName(),
                request.getJobTitle(),
                request.getPermissions());

        return ResponseEntity.ok(staffMapper.toResponse(updateStaffPermissionsHandler.handle(command)));
    }

    @PreAuthorize("hasRole('PHARMACY')")
    @PostMapping("/{staffId}/deactivate")
    public ResponseEntity<MessageResponse> deactivateStaff(
            @AuthenticationPrincipal GatewayAuthenticatedPrincipal principal,
            @PathVariable UUID staffId) {

        setStaffActiveHandler.handle(new SetStaffActiveCommand(principal.getUserId(), staffId, false));
        return ResponseEntity.ok(new MessageResponse("Staff member deactivated"));
    }

    @PreAuthorize("hasRole('PHARMACY')")
    @PostMapping("/{staffId}/activate")
    public ResponseEntity<MessageResponse> activateStaff(
            @AuthenticationPrincipal GatewayAuthenticatedPrincipal principal,
            @PathVariable UUID staffId) {

        setStaffActiveHandler.handle(new SetStaffActiveCommand(principal.getUserId(), staffId, true));
        return ResponseEntity.ok(new MessageResponse("Staff member activated"));
    }
}
