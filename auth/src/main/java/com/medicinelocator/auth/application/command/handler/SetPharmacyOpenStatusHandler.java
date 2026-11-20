package com.medicinelocator.auth.application.command.handler;

import com.medicinelocator.auth.application.command.SetPharmacyOpenStatusCommand;
import com.medicinelocator.auth.application.service.PharmacyService;
import com.medicinelocator.auth.application.service.SearchIndexNotifier;
import com.medicinelocator.auth.domain.enums.AccountStatus;
import com.medicinelocator.auth.domain.exception.PharmacyNotApprovedException;
import com.medicinelocator.auth.domain.model.Pharmacy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SetPharmacyOpenStatusHandler {

    private static final Logger log = LoggerFactory.getLogger(SetPharmacyOpenStatusHandler.class);

    private final PharmacyService pharmacyService;
    private final SearchIndexNotifier searchIndexNotifier;

    public SetPharmacyOpenStatusHandler(PharmacyService pharmacyService,
                                        SearchIndexNotifier searchIndexNotifier) {
        this.pharmacyService = pharmacyService;
        this.searchIndexNotifier = searchIndexNotifier;
    }

    @Transactional
    public boolean handle(SetPharmacyOpenStatusCommand command) {
        Pharmacy pharmacy = pharmacyService.findById(command.getPharmacyId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pharmacy not found with id: " + command.getPharmacyId()));

        // Only an approved, active pharmacy is allowed to be visible to customers,
        // so only such a pharmacy may open/close its shop.
        if (!pharmacy.isApproved()) {
            throw new PharmacyNotApprovedException(
                    "Pharmacy must be approved before it can open or close");
        }
        if (pharmacy.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new PharmacyNotApprovedException(
                    "Pharmacy account is not active");
        }

        if (command.isOpen()) {
            pharmacy.openShop();
        } else {
            pharmacy.closeShop();
        }

        pharmacyService.update(pharmacy);

        // Propagate to the search index so customer search reflects it immediately.
        searchIndexNotifier.updatePharmacyOpenStatus(pharmacy.getId(), pharmacy.isOpen());

        log.info("Pharmacy open-status changed: pharmacyId={} open={}",
                pharmacy.getId(), pharmacy.isOpen());

        return pharmacy.isOpen();
    }
}
