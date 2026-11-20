package com.medicinelocator.paymentservice.application.command.handler;

import com.medicinelocator.paymentservice.application.command.StartTrialCommand;
import com.medicinelocator.paymentservice.application.service.NotificationNotifier;
import com.medicinelocator.paymentservice.application.service.PaymentNotifications;
import com.medicinelocator.paymentservice.application.service.PharmacyAccessService;
import com.medicinelocator.paymentservice.application.service.SubscriptionRepository;
import com.medicinelocator.paymentservice.domain.model.Subscription;
import com.medicinelocator.paymentservice.infrastructure.config.SubscriptionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Starts a free trial for a pharmacy (triggered when the admin approves it).
 * Idempotent: does nothing if the pharmacy already has any subscription record.
 */
@Component
public class StartTrialHandler {

    private static final Logger log = LoggerFactory.getLogger(StartTrialHandler.class);

    private final SubscriptionRepository subscriptionRepository;
    private final PharmacyAccessService pharmacyAccessService;
    private final NotificationNotifier notifier;
    private final SubscriptionProperties properties;

    public StartTrialHandler(SubscriptionRepository subscriptionRepository,
                             PharmacyAccessService pharmacyAccessService,
                             NotificationNotifier notifier,
                             SubscriptionProperties properties) {
        this.subscriptionRepository = subscriptionRepository;
        this.pharmacyAccessService = pharmacyAccessService;
        this.notifier = notifier;
        this.properties = properties;
    }

    @Transactional
    public Subscription handle(StartTrialCommand command) {
        if (!subscriptionRepository.findByPharmacy(command.getPharmacyId()).isEmpty()) {
            log.info("Trial skipped; pharmacy {} already has a subscription history",
                    command.getPharmacyId());
            return subscriptionRepository.findCurrentForPharmacy(command.getPharmacyId()).orElse(null);
        }

        Subscription trial = subscriptionRepository.save(
                Subscription.createTrial(command.getPharmacyId(), properties.getTrialDays()));

        pharmacyAccessService.pushSubscriptionState(
                trial.getPharmacyId(), "TRIAL", trial.getEndDate());

        notifier.notify(
                trial.getPharmacyId(),
                PaymentNotifications.PHARMACY,
                PaymentNotifications.TRIAL_STARTED,
                "Free trial started",
                "Your " + properties.getTrialDays() + "-day free trial is active until "
                        + trial.getEndDate().toLocalDate()
                        + ". Subscribe before it ends to keep your pharmacy listed.",
                PaymentNotifications.LINK_SUBSCRIPTION);

        log.info("Trial started for pharmacy {} until {}", trial.getPharmacyId(), trial.getEndDate());
        return trial;
    }
}
