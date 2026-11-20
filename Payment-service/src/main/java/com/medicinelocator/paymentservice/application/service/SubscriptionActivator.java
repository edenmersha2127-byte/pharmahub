package com.medicinelocator.paymentservice.application.service;

import com.medicinelocator.paymentservice.domain.model.Coupon;
import com.medicinelocator.paymentservice.domain.model.Payment;
import com.medicinelocator.paymentservice.domain.model.Subscription;
import com.medicinelocator.paymentservice.infrastructure.config.SubscriptionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Shared "a payment succeeded → turn the subscription on" logic, used by both
 * the paid-checkout confirmation and the 100%-coupon immediate path.
 */
@Component
public class SubscriptionActivator {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionActivator.class);

    private final SubscriptionRepository subscriptionRepository;
    private final CouponRepository couponRepository;
    private final PharmacyAccessService pharmacyAccessService;
    private final NotificationNotifier notifier;
    private final SubscriptionProperties properties;

    public SubscriptionActivator(SubscriptionRepository subscriptionRepository,
                                 CouponRepository couponRepository,
                                 PharmacyAccessService pharmacyAccessService,
                                 NotificationNotifier notifier,
                                 SubscriptionProperties properties) {
        this.subscriptionRepository = subscriptionRepository;
        this.couponRepository = couponRepository;
        this.pharmacyAccessService = pharmacyAccessService;
        this.notifier = notifier;
        this.properties = properties;
    }

    public void activatePaid(Subscription subscription, Payment payment) {
        int durationDays = properties.planFor(subscription.getPlan()).getDurationDays();

        // Extend from any still-valid access period (e.g. an ongoing trial) so
        // no time is lost.
        LocalDateTime currentAccessUntil = subscriptionRepository
                .findCurrentForPharmacy(subscription.getPharmacyId())
                .map(Subscription::getEndDate)
                .orElse(null);

        subscription.activate(durationDays, currentAccessUntil);
        Subscription saved = subscriptionRepository.save(subscription);

        // Redeem the coupon (once, on success) if one was used.
        if (payment.getCouponCode() != null && !payment.getCouponCode().isBlank()) {
            Optional<Coupon> coupon = couponRepository.findByCode(payment.getCouponCode());
            coupon.ifPresent(c -> {
                c.redeem();
                couponRepository.save(c);
            });
        }

        pharmacyAccessService.pushSubscriptionState(
                saved.getPharmacyId(), "ACTIVE", saved.getEndDate());

        notifier.notify(
                saved.getPharmacyId(),
                PaymentNotifications.PHARMACY,
                PaymentNotifications.SUBSCRIPTION_ACTIVATED,
                "Subscription active",
                "Your subscription is active until " + saved.getEndDate().toLocalDate()
                        + ". Your pharmacy is now visible in search and you can manage inventory.",
                PaymentNotifications.LINK_SUBSCRIPTION);

        log.info("Subscription activated: pharmacyId={} until={}",
                saved.getPharmacyId(), saved.getEndDate());
    }
}
