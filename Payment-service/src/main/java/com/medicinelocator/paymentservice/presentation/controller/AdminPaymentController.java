package com.medicinelocator.paymentservice.presentation.controller;

import com.medicinelocator.paymentservice.application.command.CreateCouponCommand;
import com.medicinelocator.paymentservice.application.command.handler.CreateCouponHandler;
import com.medicinelocator.paymentservice.application.dto.request.CreateCouponRequest;
import com.medicinelocator.paymentservice.application.dto.response.CouponResponse;
import com.medicinelocator.paymentservice.application.dto.response.PaymentListResponse;
import com.medicinelocator.paymentservice.application.dto.response.SubscriptionListResponse;
import com.medicinelocator.paymentservice.application.query.handler.CouponQueryHandler;
import com.medicinelocator.paymentservice.application.query.handler.PaymentQueryHandler;
import com.medicinelocator.paymentservice.application.query.handler.SubscriptionQueryHandler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    private final SubscriptionQueryHandler subscriptionQueryHandler;
    private final PaymentQueryHandler paymentQueryHandler;
    private final CouponQueryHandler couponQueryHandler;
    private final CreateCouponHandler createCouponHandler;

    public AdminPaymentController(SubscriptionQueryHandler subscriptionQueryHandler,
                                  PaymentQueryHandler paymentQueryHandler,
                                  CouponQueryHandler couponQueryHandler,
                                  CreateCouponHandler createCouponHandler) {
        this.subscriptionQueryHandler = subscriptionQueryHandler;
        this.paymentQueryHandler = paymentQueryHandler;
        this.couponQueryHandler = couponQueryHandler;
        this.createCouponHandler = createCouponHandler;
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<SubscriptionListResponse> allSubscriptions(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(subscriptionQueryHandler.findAll(page, size));
    }

    @GetMapping("/payments")
    public ResponseEntity<PaymentListResponse> allPayments(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(paymentQueryHandler.findAll(page, size));
    }

    @GetMapping("/coupons")
    public ResponseEntity<List<CouponResponse>> listCoupons() {
        return ResponseEntity.ok(couponQueryHandler.findAll());
    }

    @PostMapping("/coupons")
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        CouponResponse response = CouponResponse.from(createCouponHandler.handle(new CreateCouponCommand(
                request.getCode(), request.getDiscountType(), request.getDiscountValue(),
                request.getMaxRedemptions(), request.getExpiresAt())));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
