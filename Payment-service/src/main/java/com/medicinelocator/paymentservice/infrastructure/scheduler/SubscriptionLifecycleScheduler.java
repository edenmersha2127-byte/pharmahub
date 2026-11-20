package com.medicinelocator.paymentservice.infrastructure.scheduler;

import com.medicinelocator.paymentservice.application.service.NotificationNotifier;
import com.medicinelocator.paymentservice.application.service.PaymentNotifications;
import com.medicinelocator.paymentservice.application.service.PharmacyAccessService;
import com.medicinelocator.paymentservice.application.service.SubscriptionRepository;
import com.medicinelocator.paymentservice.domain.model.Subscription;
import com.medicinelocator.paymentservice.infrastructure.config.SubscriptionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Daily jobs that expire elapsed subscriptions (trial or paid) and remind
 * pharmacies a few days before expiry. On expiry the pharmacy is pushed to auth
 * as EXPIRED (hidden from search, inventory-add blocked) and notified to subscribe.
 */
@Component
@ConditionalOnProperty(prefix = "subscription", name = "scheduler-enabled",
        havingValue = "true", matchIfMissing = true)
public class SubscriptionLifecycleScheduler {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionLifecycleScheduler.class);

    private final SubscriptionRepository subscriptionRepository;
    private final PharmacyAccessService pharmacyAccessService;
    private final NotificationNotifier notifier;
    private final SubscriptionProperties properties;

    public SubscriptionLifecycleScheduler(SubscriptionRepository subscriptionRepository,
                                          PharmacyAccessService pharmacyAccessService,
                                          NotificationNotifier notifier,
                                          SubscriptionProperties properties) {
        this.subscriptionRepository = subscriptionRepository;
        this.pharmacyAccessService = pharmacyAccessService;
        this.notifier = notifier;
        this.properties = properties;
    }

    /** Expire subscriptions whose period has elapsed. Runs daily at 01:00. */
    @Scheduled(cron = "${subscription.expiry-cron:0 0 1 * * *}")
    public void expireElapsed() {
        List<Subscription> expired = subscriptionRepository.findExpired(LocalDateTime.now());
        if (expired.isEmpty()) {
            return;
        }
        log.info("Subscription expiry job: {} subscription(s) to expire", expired.size());
        for (Subscription subscription : expired) {
            try {
                subscription.expire();
                subscriptionRepository.save(subscription);
                pharmacyAccessService.pushSubscriptionState(
                        subscription.getPharmacyId(), "EXPIRED", null);
                notifier.notify(
                        subscription.getPharmacyId(),
                        PaymentNotifications.PHARMACY,
                        PaymentNotifications.SUBSCRIPTION_EXPIRED,
                        "Subscription expired",
                        "Your subscription has ended. Your pharmacy is now hidden from customer search "
                                + "and you cannot add inventory. Subscribe to restore access.",
                        PaymentNotifications.LINK_SUBSCRIPTION);
            } catch (Exception e) {
                log.error("Failed to expire subscription {}: {}", subscription.getId(), e.getMessage(), e);
            }
        }
    }

    /** Remind pharmacies whose subscription expires soon. Runs daily at 01:30. */
    @Scheduled(cron = "${subscription.reminder-cron:0 30 1 * * *}")
    public void remindExpiringSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime until = now.plusDays(properties.getReminderDaysBefore());
        List<Subscription> expiring = subscriptionRepository.findExpiringSoon(now, until);
        if (expiring.isEmpty()) {
            return;
        }
        log.info("Subscription reminder job: {} subscription(s) expiring soon", expiring.size());
        for (Subscription subscription : expiring) {
            try {
                notifier.notify(
                        subscription.getPharmacyId(),
                        PaymentNotifications.PHARMACY,
                        PaymentNotifications.SUBSCRIPTION_EXPIRING_SOON,
                        "Subscription expiring soon",
                        "Your subscription expires on " + subscription.getEndDate().toLocalDate()
                                + ". Renew now to avoid being hidden from customer search.",
                        PaymentNotifications.LINK_SUBSCRIPTION);
                subscription.markReminderSent();
                subscriptionRepository.save(subscription);
            } catch (Exception e) {
                log.error("Failed to remind subscription {}: {}", subscription.getId(), e.getMessage(), e);
            }
        }
    }
}
