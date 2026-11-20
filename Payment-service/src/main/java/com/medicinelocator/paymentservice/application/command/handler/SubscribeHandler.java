package com.medicinelocator.paymentservice.application.command.handler;

import com.medicinelocator.paymentservice.application.command.SubscribeCommand;
import com.medicinelocator.paymentservice.application.dto.response.CheckoutResponse;
import com.medicinelocator.paymentservice.application.service.*;
import com.medicinelocator.paymentservice.domain.exception.CouponException;
import com.medicinelocator.paymentservice.domain.model.Coupon;
import com.medicinelocator.paymentservice.domain.model.Payment;
import com.medicinelocator.paymentservice.domain.model.Subscription;
import com.medicinelocator.paymentservice.infrastructure.config.SubscriptionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Starts a paid subscription: prices the plan, applies an optional coupon,
 * creates a PENDING subscription + payment, and opens a Chapa checkout.
 * A 100%-off coupon activates immediately with no gateway round-trip.
 */
@Component
public class SubscribeHandler {

    private static final Logger log = LoggerFactory.getLogger(SubscribeHandler.class);

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final CouponRepository couponRepository;
    private final PaymentGateway paymentGateway;
    private final PharmacyAccessService pharmacyAccessService;
    private final SubscriptionActivator activator;
    private final SubscriptionProperties properties;
    private final String currency;

    public SubscribeHandler(SubscriptionRepository subscriptionRepository,
                            PaymentRepository paymentRepository,
                            CouponRepository couponRepository,
                            PaymentGateway paymentGateway,
                            PharmacyAccessService pharmacyAccessService,
                            SubscriptionActivator activator,
                            SubscriptionProperties properties,
                            @Value("${chapa.currency:ETB}") String currency) {
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
        this.couponRepository = couponRepository;
        this.paymentGateway = paymentGateway;
        this.pharmacyAccessService = pharmacyAccessService;
        this.activator = activator;
        this.properties = properties;
        this.currency = currency;
    }

    @Transactional
    public CheckoutResponse handle(SubscribeCommand command) {
        PharmacyAccessService.PharmacyInfo pharmacy = pharmacyAccessService
                .getPharmacy(command.getPharmacyId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pharmacy not found or auth service unavailable."));

        BigDecimal price = properties.planFor(command.getPlan()).getPrice();

        String couponCode = null;
        BigDecimal amount = price;
        if (command.getCouponCode() != null && !command.getCouponCode().isBlank()) {
            Coupon coupon = couponRepository.findByCode(command.getCouponCode().trim().toUpperCase())
                    .orElseThrow(() -> new CouponException("Coupon code not found."));
            coupon.assertUsable();
            amount = coupon.apply(price);
            couponCode = coupon.getCode();
        }

        Subscription subscription = subscriptionRepository.save(
                Subscription.createPending(command.getPharmacyId(), command.getPlan(), amount));

        String txRef = "MEDILOC-" + UUID.randomUUID();

        // 100%-off coupon: no gateway needed, activate right away.
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            Payment payment = Payment.createPending(
                    subscription.getId(), command.getPharmacyId(), txRef, amount, currency, couponCode);
            payment.markSuccess("{\"note\":\"fully covered by coupon\"}");
            paymentRepository.save(payment);

            activator.activatePaid(subscription, payment);
            log.info("Subscription activated via 100% coupon: pharmacyId={}", command.getPharmacyId());
            return new CheckoutResponse(subscription.getId(), txRef, amount, null, "ACTIVE");
        }

        String checkoutUrl = paymentGateway.initializeCheckout(new PaymentGateway.CheckoutRequest(
                txRef, amount, pharmacy.email(),
                pharmacy.name() != null ? pharmacy.name() : "Pharmacy", "Pharmacy"));

        Payment payment = Payment.createPending(
                subscription.getId(), command.getPharmacyId(), txRef, amount, currency, couponCode);
        payment.setCheckoutUrl(checkoutUrl);
        paymentRepository.save(payment);

        log.info("Checkout created: pharmacyId={} plan={} amount={} txRef={}",
                command.getPharmacyId(), command.getPlan(), amount, txRef);
        return new CheckoutResponse(subscription.getId(), txRef, amount, checkoutUrl, "PENDING");
    }
}
