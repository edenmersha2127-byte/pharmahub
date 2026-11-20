package com.medicinelocator.auth.application.command.handler;

import com.medicinelocator.auth.application.command.RejectPharmacyCommand;
import com.medicinelocator.auth.application.service.NotificationNotifier;
import com.medicinelocator.auth.application.service.PharmacyService;
import com.medicinelocator.auth.domain.exception.PharmacyNotFoundException;
import com.medicinelocator.auth.domain.model.Pharmacy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RejectPharmacyHandler {

    private static final Logger log = LoggerFactory.getLogger(RejectPharmacyHandler.class);

    private final PharmacyService pharmacyService;
    private final NotificationNotifier notificationNotifier;

    public RejectPharmacyHandler(PharmacyService pharmacyService,
                                 NotificationNotifier notificationNotifier) {
        this.pharmacyService = pharmacyService;
        this.notificationNotifier = notificationNotifier;
    }


    @Transactional
    public Pharmacy handle(RejectPharmacyCommand command) {
        Pharmacy pharmacy = pharmacyService.findById(command.getPharmacyId())
                .orElseThrow(() -> new PharmacyNotFoundException(command.getPharmacyId()));

        pharmacy.reject();
        Pharmacy updated = pharmacyService.update(pharmacy);

        log.info("Pharmacy rejected: pharmacyId={} email={}",
                updated.getId(), updated.getEmail());

        notificationNotifier.notify(
                updated.getId(),
                "PHARMACY",
                "PHARMACY_REJECTED",
                "Pharmacy registration rejected",
                "Your pharmacy '" + updated.getPharmacyName()
                        + "' registration was not approved. Please contact support for details.",
                "/pharmacy/dashboard");

        return updated;
    }
}