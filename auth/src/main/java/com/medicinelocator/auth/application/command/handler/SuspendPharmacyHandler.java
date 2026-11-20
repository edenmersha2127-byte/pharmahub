package com.medicinelocator.auth.application.command.handler;

import com.medicinelocator.auth.application.command.SuspendPharmacyCommand;
import com.medicinelocator.auth.application.service.PharmacyService;
import com.medicinelocator.auth.application.service.SearchIndexNotifier;
import com.medicinelocator.auth.domain.exception.PharmacyNotFoundException;
import com.medicinelocator.auth.domain.model.Pharmacy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Suspends a pharmacy for a number of days (invoked by the Report Service via
 * the internal endpoint). Hides the pharmacy from search by pushing a closed
 * open-status to the Search Service.
 */
@Component
public class SuspendPharmacyHandler {

    private static final Logger log = LoggerFactory.getLogger(SuspendPharmacyHandler.class);

    private final PharmacyService pharmacyService;
    private final SearchIndexNotifier searchIndexNotifier;

    public SuspendPharmacyHandler(PharmacyService pharmacyService,
                                  SearchIndexNotifier searchIndexNotifier) {
        this.pharmacyService = pharmacyService;
        this.searchIndexNotifier = searchIndexNotifier;
    }

    @Transactional
    public Pharmacy handle(SuspendPharmacyCommand command) {
        Pharmacy pharmacy = pharmacyService.findById(command.getPharmacyId())
                .orElseThrow(() -> new PharmacyNotFoundException(command.getPharmacyId()));

        pharmacy.suspend(command.getDays());
        Pharmacy updated = pharmacyService.update(pharmacy);

        log.info("Pharmacy suspended: pharmacyId={} until={}",
                updated.getId(), updated.getSuspendedUntil());

        // Hide from customer search while suspended (best-effort).
        searchIndexNotifier.updatePharmacyOpenStatus(updated.getId(), false);

        return updated;
    }
}
