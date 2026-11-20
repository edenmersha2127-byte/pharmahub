package com.medicinelocator.auth.application.command.handler;

import com.medicinelocator.auth.application.command.ApprovePharmacyCommand;
import com.medicinelocator.auth.application.service.NotificationNotifier;
import com.medicinelocator.auth.application.service.PharmacyService;
import com.medicinelocator.auth.application.service.SubscriptionTrialNotifier;
import com.medicinelocator.auth.domain.exception.PharmacyNotFoundException;
import com.medicinelocator.auth.domain.model.Pharmacy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ApprovePharmacyHandler {

    private static final Logger log = LoggerFactory.getLogger(ApprovePharmacyHandler.class);

    private final PharmacyService pharmacyService;
    private final NotificationNotifier notificationNotifier;
    private final SubscriptionTrialNotifier subscriptionTrialNotifier;

    public ApprovePharmacyHandler(PharmacyService pharmacyService,
                                  NotificationNotifier notificationNotifier,
                                  SubscriptionTrialNotifier subscriptionTrialNotifier) {
        this.pharmacyService = pharmacyService;
        this.notificationNotifier = notificationNotifier;
        this.subscriptionTrialNotifier = subscriptionTrialNotifier;
    }


    @Transactional
    public Pharmacy handle(ApprovePharmacyCommand command) {
        Pharmacy pharmacy = pharmacyService.findById(command.getPharmacyId())
                .orElseThrow(() -> new PharmacyNotFoundException(command.getPharmacyId()));

        pharmacy.approve();
        Pharmacy updated = pharmacyService.update(pharmacy);

        log.info("Pharmacy approved: pharmacyId={} email={}",
                updated.getId(), updated.getEmail());

        notificationNotifier.notify(
                updated.getId(),
                "PHARMACY",
                "PHARMACY_APPROVED",
                "Pharmacy approved",
                "Great news! Your pharmacy '" + updated.getPharmacyName()
                        + "' has been approved. You can now manage your inventory.",
                "/pharmacy/dashboard");

        // Kick off the free trial in the Payment Service (best-effort).
        subscriptionTrialNotifier.startTrial(updated.getId());

        return updated;
    }
}