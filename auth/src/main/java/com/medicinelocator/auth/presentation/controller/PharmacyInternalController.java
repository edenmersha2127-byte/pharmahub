package com.medicinelocator.auth.presentation.controller;

import com.medicinelocator.auth.application.command.AddPharmacyStrikeCommand;
import com.medicinelocator.auth.application.command.LiftPharmacySuspensionCommand;
import com.medicinelocator.auth.application.command.SetPharmacySubscriptionCommand;
import com.medicinelocator.auth.application.command.SuspendPharmacyCommand;
import com.medicinelocator.auth.application.command.handler.AddPharmacyStrikeHandler;
import com.medicinelocator.auth.application.command.handler.LiftPharmacySuspensionHandler;
import com.medicinelocator.auth.application.command.handler.SetPharmacySubscriptionHandler;
import com.medicinelocator.auth.application.command.handler.SuspendPharmacyHandler;
import com.medicinelocator.auth.application.dto.response.PharmacyProfileResponse;
import com.medicinelocator.auth.application.dto.response.PharmacySummaryResponse;
import com.medicinelocator.auth.application.dto.response.SubscriptionAccessResponse;
import com.medicinelocator.auth.application.query.GetAllPharmaciesQuery;
import com.medicinelocator.auth.application.query.GetPharmacyProfileQuery;
import com.medicinelocator.auth.application.query.handler.GetAllPharmaciesHandler;
import com.medicinelocator.auth.application.query.handler.GetPharmacyProfileHandler;
import com.medicinelocator.auth.application.service.PharmacyService;
import com.medicinelocator.auth.domain.exception.PharmacyNotFoundException;
import com.medicinelocator.auth.domain.model.Pharmacy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Internal REST endpoint consumed by the Inventory Service.
 *
 * Route: GET /internal/pharmacies/{pharmacyId}
 *
 * This endpoint is NOT exposed through the public API Gateway.
 * It is a service-to-service endpoint only.
 * In production, restrict access to this path at the network/gateway level.
 */
@RestController
@RequestMapping("/internal/pharmacies")
public class PharmacyInternalController {

    private static final Logger log = LoggerFactory.getLogger(PharmacyInternalController.class);

    private final GetPharmacyProfileHandler getPharmacyProfileHandler;
    private final GetAllPharmaciesHandler getAllPharmaciesHandler;
    private final SuspendPharmacyHandler suspendPharmacyHandler;
    private final AddPharmacyStrikeHandler addPharmacyStrikeHandler;
    private final LiftPharmacySuspensionHandler liftPharmacySuspensionHandler;
    private final SetPharmacySubscriptionHandler setPharmacySubscriptionHandler;
    private final PharmacyService pharmacyService;

    public PharmacyInternalController(GetPharmacyProfileHandler getPharmacyProfileHandler,
                                      GetAllPharmaciesHandler getAllPharmaciesHandler,
                                      SuspendPharmacyHandler suspendPharmacyHandler,
                                      AddPharmacyStrikeHandler addPharmacyStrikeHandler,
                                      LiftPharmacySuspensionHandler liftPharmacySuspensionHandler,
                                      SetPharmacySubscriptionHandler setPharmacySubscriptionHandler,
                                      PharmacyService pharmacyService) {
        this.getPharmacyProfileHandler = getPharmacyProfileHandler;
        this.getAllPharmaciesHandler = getAllPharmaciesHandler;
        this.suspendPharmacyHandler = suspendPharmacyHandler;
        this.addPharmacyStrikeHandler = addPharmacyStrikeHandler;
        this.liftPharmacySuspensionHandler = liftPharmacySuspensionHandler;
        this.setPharmacySubscriptionHandler = setPharmacySubscriptionHandler;
        this.pharmacyService = pharmacyService;
    }

    /**
     * GET /internal/pharmacies
     *
     * Returns the full pharmacy directory (id, name, email, status).
     * Consumed by the Notification Service to resolve the audience of
     * pharmacy-wide notifications (e.g. the nightly medicine-update reminder).
     */
    @GetMapping
    public ResponseEntity<List<PharmacySummaryResponse>> getAllPharmacies() {
        log.debug("Internal pharmacy directory request");
        List<PharmacySummaryResponse> pharmacies =
                getAllPharmaciesHandler.handle(new GetAllPharmaciesQuery());
        return ResponseEntity.ok(pharmacies);
    }

    /**
     * GET /internal/pharmacies/{pharmacyId}
     *
     * Returns the pharmacy's profile data needed by the Inventory Service
     * to enrich medicine records before sending them to the Search Service.
     */
    @GetMapping("/{pharmacyId}")
    public ResponseEntity<PharmacyProfileResponse> getPharmacyProfile(
            @PathVariable UUID pharmacyId) {
        log.debug("Internal pharmacy profile request: pharmacyId={}", pharmacyId);
        PharmacyProfileResponse response = getPharmacyProfileHandler.handle(
                new GetPharmacyProfileQuery(pharmacyId));
        return ResponseEntity.ok(response);
    }

    /**
     * POST /internal/pharmacies/{pharmacyId}/suspend?days=3
     * Temporarily suspend a pharmacy. Called by the Report Service.
     */
    @PostMapping("/{pharmacyId}/suspend")
    public ResponseEntity<Map<String, String>> suspend(
            @PathVariable UUID pharmacyId,
            @RequestParam(defaultValue = "3") int days) {
        log.info("Internal suspend request: pharmacyId={} days={}", pharmacyId, days);
        suspendPharmacyHandler.handle(new SuspendPharmacyCommand(pharmacyId, days));
        return ResponseEntity.ok(Map.of("status", "suspended", "pharmacyId", pharmacyId.toString()));
    }

    /**
     * POST /internal/pharmacies/{pharmacyId}/strike
     * Record a formal warning (strike). Called by the Report Service.
     */
    @PostMapping("/{pharmacyId}/strike")
    public ResponseEntity<Map<String, String>> strike(@PathVariable UUID pharmacyId) {
        log.info("Internal strike request: pharmacyId={}", pharmacyId);
        addPharmacyStrikeHandler.handle(new AddPharmacyStrikeCommand(pharmacyId));
        return ResponseEntity.ok(Map.of("status", "struck", "pharmacyId", pharmacyId.toString()));
    }

    /**
     * POST /internal/pharmacies/{pharmacyId}/lift-suspension
     * Lift an active suspension. Called by the Report Service.
     */
    @PostMapping("/{pharmacyId}/lift-suspension")
    public ResponseEntity<Map<String, String>> liftSuspension(@PathVariable UUID pharmacyId) {
        log.info("Internal lift-suspension request: pharmacyId={}", pharmacyId);
        liftPharmacySuspensionHandler.handle(new LiftPharmacySuspensionCommand(pharmacyId));
        return ResponseEntity.ok(Map.of("status", "lifted", "pharmacyId", pharmacyId.toString()));
    }

    /**
     * POST /internal/pharmacies/{pharmacyId}/subscription?status=ACTIVE&until=2026-01-01T00:00
     * Set the pharmacy's subscription state (called by the Payment Service).
     */
    @PostMapping("/{pharmacyId}/subscription")
    public ResponseEntity<Map<String, String>> setSubscription(
            @PathVariable UUID pharmacyId,
            @RequestParam String status,
            @RequestParam(required = false) String until) {
        LocalDateTime subscribedUntil = (until != null && !until.isBlank())
                ? LocalDateTime.parse(until) : null;
        log.info("Internal set-subscription request: pharmacyId={} status={} until={}",
                pharmacyId, status, subscribedUntil);
        setPharmacySubscriptionHandler.handle(
                new SetPharmacySubscriptionCommand(pharmacyId, status, subscribedUntil));
        return ResponseEntity.ok(Map.of("status", status, "pharmacyId", pharmacyId.toString()));
    }

    /**
     * GET /internal/pharmacies/{pharmacyId}/subscription
     * Whether the pharmacy currently has subscription access (used by Inventory).
     */
    @GetMapping("/{pharmacyId}/subscription")
    public ResponseEntity<SubscriptionAccessResponse> getSubscription(@PathVariable UUID pharmacyId) {
        Pharmacy pharmacy = pharmacyService.findById(pharmacyId)
                .orElseThrow(() -> new PharmacyNotFoundException(pharmacyId));
        return ResponseEntity.ok(new SubscriptionAccessResponse(
                pharmacy.getId(),
                pharmacy.isSubscriptionActive(),
                pharmacy.getSubscriptionStatus(),
                pharmacy.getSubscribedUntil()));
    }
}