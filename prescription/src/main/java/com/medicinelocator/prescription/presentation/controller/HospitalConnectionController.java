package com.medicinelocator.prescription.presentation.controller;

import com.medicinelocator.prescription.application.dto.request.CreateHospitalConnectionRequest;
import com.medicinelocator.prescription.application.dto.response.HospitalConnectionResponse;
import com.medicinelocator.prescription.application.verification.HospitalConnectionOnboardingService;
import com.medicinelocator.prescription.domain.exception.InvalidPrescriptionException;
import com.medicinelocator.prescription.domain.model.HospitalConnection;
import com.medicinelocator.prescription.infrastructure.security.CurrentUser;
import com.medicinelocator.prescription.infrastructure.security.CurrentUserProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin-only management of integrated hospitals (the verification directory).
 */
@RestController
@RequestMapping("/api/hospital-connections")
public class HospitalConnectionController {

    private final HospitalConnectionOnboardingService onboardingService;
    private final CurrentUserProvider currentUserProvider;

    public HospitalConnectionController(HospitalConnectionOnboardingService onboardingService,
                                        CurrentUserProvider currentUserProvider) {
        this.onboardingService = onboardingService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    public ResponseEntity<HospitalConnectionResponse> create(
            @Valid @RequestBody CreateHospitalConnectionRequest request) {
        requireAdmin();
        HospitalConnection saved = onboardingService.onboard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping
    public ResponseEntity<List<HospitalConnectionResponse>> list() {
        requireAdmin();
        List<HospitalConnectionResponse> connections = onboardingService.list().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(connections);
    }

    private void requireAdmin() {
        CurrentUser currentUser = currentUserProvider.getCurrentUser();
        if (!currentUser.isAdmin()) {
            throw new InvalidPrescriptionException("Access denied: only ADMIN can manage hospital connections");
        }
    }

    private HospitalConnectionResponse toResponse(HospitalConnection c) {
        return new HospitalConnectionResponse(
                c.getId(), c.getName(), c.getEmail(), c.getHospitalId(), c.getBaseUrl(), c.isActive());
    }
}
