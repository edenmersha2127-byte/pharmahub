package com.medicinelocator.paymentservice.presentation.controller;

import com.medicinelocator.paymentservice.application.command.StartTrialCommand;
import com.medicinelocator.paymentservice.application.command.handler.StartTrialHandler;
import com.medicinelocator.paymentservice.application.dto.response.SubscriptionStatusResponse;
import com.medicinelocator.paymentservice.application.query.handler.SubscriptionQueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Internal service-to-service endpoints (not exposed via the gateway).
 *
 *  - POST /internal/subscriptions/trial/{pharmacyId}  (auth calls on approval)
 *  - GET  /internal/subscriptions/{pharmacyId}        (subscription status)
 */
@RestController
@RequestMapping("/internal/subscriptions")
public class SubscriptionInternalController {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionInternalController.class);

    private final StartTrialHandler startTrialHandler;
    private final SubscriptionQueryHandler subscriptionQueryHandler;

    public SubscriptionInternalController(StartTrialHandler startTrialHandler,
                                          SubscriptionQueryHandler subscriptionQueryHandler) {
        this.startTrialHandler = startTrialHandler;
        this.subscriptionQueryHandler = subscriptionQueryHandler;
    }

    @PostMapping("/trial/{pharmacyId}")
    public ResponseEntity<Void> startTrial(@PathVariable UUID pharmacyId) {
        log.info("Internal start-trial request: pharmacyId={}", pharmacyId);
        startTrialHandler.handle(new StartTrialCommand(pharmacyId));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{pharmacyId}")
    public ResponseEntity<SubscriptionStatusResponse> status(@PathVariable UUID pharmacyId) {
        return ResponseEntity.ok(subscriptionQueryHandler.status(pharmacyId));
    }
}
