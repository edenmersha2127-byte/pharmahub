package com.medicinelocator.auth.application.command.handler;

import com.medicinelocator.auth.application.command.SetPharmacySubscriptionCommand;
import com.medicinelocator.auth.application.service.PharmacyService;
import com.medicinelocator.auth.application.service.SearchIndexNotifier;
import com.medicinelocator.auth.domain.exception.PharmacyNotFoundException;
import com.medicinelocator.auth.domain.model.Pharmacy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Applies subscription state pushed by the Payment Service and syncs search
 * visibility: an active/trial pharmacy shows in search per its open status; an
 * expired one is hidden.
 */
@Component
public class SetPharmacySubscriptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SetPharmacySubscriptionHandler.class);

    private final PharmacyService pharmacyService;
    private final SearchIndexNotifier searchIndexNotifier;

    public SetPharmacySubscriptionHandler(PharmacyService pharmacyService,
                                          SearchIndexNotifier searchIndexNotifier) {
        this.pharmacyService = pharmacyService;
        this.searchIndexNotifier = searchIndexNotifier;
    }

    @Transactional
    public Pharmacy handle(SetPharmacySubscriptionCommand command) {
        Pharmacy pharmacy = pharmacyService.findById(command.getPharmacyId())
                .orElseThrow(() -> new PharmacyNotFoundException(command.getPharmacyId()));

        pharmacy.applySubscription(command.getStatus(), command.getSubscribedUntil());
        Pharmacy updated = pharmacyService.update(pharmacy);

        // Hide from search when the subscription is not active; otherwise reflect
        // the pharmacy's real open/closed state.
        boolean visible = updated.isSubscriptionActive() && updated.isOpen();
        searchIndexNotifier.updatePharmacyOpenStatus(updated.getId(), visible);

        log.info("Subscription state set: pharmacyId={} status={} until={} searchVisible={}",
                updated.getId(), command.getStatus(), command.getSubscribedUntil(), visible);
        return updated;
    }
}
