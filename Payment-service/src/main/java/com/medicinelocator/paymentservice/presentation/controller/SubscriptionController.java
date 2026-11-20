package com.medicinelocator.paymentservice.presentation.controller;

import com.medicinelocator.paymentservice.application.command.ConfirmPaymentCommand;
import com.medicinelocator.paymentservice.application.command.SubscribeCommand;
import com.medicinelocator.paymentservice.application.command.handler.ConfirmPaymentHandler;
import com.medicinelocator.paymentservice.application.command.handler.SubscribeHandler;
import com.medicinelocator.paymentservice.application.dto.request.SubscribeRequest;
import com.medicinelocator.paymentservice.application.dto.response.CheckoutResponse;
import com.medicinelocator.paymentservice.application.dto.response.CouponPreviewResponse;
import com.medicinelocator.paymentservice.application.dto.response.PaymentListResponse;
import com.medicinelocator.paymentservice.application.dto.response.PlanResponse;
import com.medicinelocator.paymentservice.application.dto.response.SubscriptionResponse;
import com.medicinelocator.paymentservice.application.query.handler.CouponPreviewHandler;
import com.medicinelocator.paymentservice.application.query.handler.PaymentQueryHandler;
import com.medicinelocator.paymentservice.application.query.handler.PlanQueryHandler;
import com.medicinelocator.paymentservice.application.query.handler.SubscriptionQueryHandler;
import com.medicinelocator.paymentservice.domain.enums.BillingCycle;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Pharmacy-facing subscription API. X-User-Id is the authenticated pharmacy.
 */
@RestController
@RequestMapping("/api/subscriptions")
@Validated
@PreAuthorize("hasRole('PHARMACY')")
public class SubscriptionController {

    private final SubscribeHandler subscribeHandler;
    private final ConfirmPaymentHandler confirmPaymentHandler;
    private final SubscriptionQueryHandler subscriptionQueryHandler;
    private final PaymentQueryHandler paymentQueryHandler;
    private final PlanQueryHandler planQueryHandler;
    private final CouponPreviewHandler couponPreviewHandler;

    public SubscriptionController(SubscribeHandler subscribeHandler,
                                  ConfirmPaymentHandler confirmPaymentHandler,
                                  SubscriptionQueryHandler subscriptionQueryHandler,
                                  PaymentQueryHandler paymentQueryHandler,
                                  PlanQueryHandler planQueryHandler,
                                  CouponPreviewHandler couponPreviewHandler) {
        this.subscribeHandler = subscribeHandler;
        this.confirmPaymentHandler = confirmPaymentHandler;
        this.subscriptionQueryHandler = subscriptionQueryHandler;
        this.paymentQueryHandler = paymentQueryHandler;
        this.planQueryHandler = planQueryHandler;
        this.couponPreviewHandler = couponPreviewHandler;
    }

    /** The available plans and their authoritative prices. */
    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponse>> plans() {
        return ResponseEntity.ok(planQueryHandler.list());
    }

    /** Preview a coupon's effect on a plan's price before checkout. */
    @GetMapping("/coupon-preview")
    public ResponseEntity<CouponPreviewResponse> couponPreview(
            @RequestParam BillingCycle plan,
            @RequestParam(required = false) String code) {
        return ResponseEntity.ok(couponPreviewHandler.preview(plan, code));
    }

    /** Start a paid subscription; returns a Chapa checkout URL to redirect to. */
    @PostMapping("/subscribe")
    public ResponseEntity<CheckoutResponse> subscribe(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody SubscribeRequest request) {

        CheckoutResponse response = subscribeHandler.handle(new SubscribeCommand(
                parseUserId(userId), request.getPlan(), request.getCouponCode()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Called by the frontend after the Chapa redirect to confirm + reflect status. */
    @PostMapping("/verify")
    public ResponseEntity<SubscriptionResponse> verify(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam @NotBlank String txRef) {

        confirmPaymentHandler.handle(new ConfirmPaymentCommand(txRef));
        return ResponseEntity.ok(subscriptionQueryHandler.findForPharmacy(parseUserId(userId)));
    }

    /** Current subscription for the pharmacy. */
    @GetMapping("/me")
    public ResponseEntity<SubscriptionResponse> mySubscription(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(subscriptionQueryHandler.findForPharmacy(parseUserId(userId)));
    }

    /** Payment history for the pharmacy. */
    @GetMapping("/payments")
    public ResponseEntity<PaymentListResponse> myPayments(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(paymentQueryHandler.findForPharmacy(parseUserId(userId), page, size));
    }

    private UUID parseUserId(String userId) {
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid or missing X-User-Id header");
        }
    }
}
