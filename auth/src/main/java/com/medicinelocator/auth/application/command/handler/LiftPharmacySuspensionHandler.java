package com.medicinelocator.auth.application.command.handler;

import com.medicinelocator.auth.application.command.LiftPharmacySuspensionCommand;
import com.medicinelocator.auth.application.service.PharmacyService;
import com.medicinelocator.auth.domain.exception.PharmacyNotFoundException;
import com.medicinelocator.auth.domain.model.Pharmacy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lifts a pharmacy's suspension and reactivates the account. Invoked by the
 * Report Service (admin action) and by the auto-lift scheduler.
 */
@Component
public class LiftPharmacySuspensionHandler {

    private static final Logger log = LoggerFactory.getLogger(LiftPharmacySuspensionHandler.class);

    private final PharmacyService pharmacyService;

    public LiftPharmacySuspensionHandler(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    @Transactional
    public Pharmacy handle(LiftPharmacySuspensionCommand command) {
        Pharmacy pharmacy = pharmacyService.findById(command.getPharmacyId())
                .orElseThrow(() -> new PharmacyNotFoundException(command.getPharmacyId()));

        pharmacy.liftSuspension();
        Pharmacy updated = pharmacyService.update(pharmacy);

        log.info("Suspension lifted for pharmacy {}", updated.getId());
        return updated;
    }
}
