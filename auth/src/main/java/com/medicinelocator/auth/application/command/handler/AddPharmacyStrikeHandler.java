package com.medicinelocator.auth.application.command.handler;

import com.medicinelocator.auth.application.command.AddPharmacyStrikeCommand;
import com.medicinelocator.auth.application.service.PharmacyService;
import com.medicinelocator.auth.domain.exception.PharmacyNotFoundException;
import com.medicinelocator.auth.domain.model.Pharmacy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records a strike (formal warning) against a pharmacy. Invoked by the Report
 * Service via the internal endpoint.
 */
@Component
public class AddPharmacyStrikeHandler {

    private static final Logger log = LoggerFactory.getLogger(AddPharmacyStrikeHandler.class);

    private final PharmacyService pharmacyService;

    public AddPharmacyStrikeHandler(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    @Transactional
    public Pharmacy handle(AddPharmacyStrikeCommand command) {
        Pharmacy pharmacy = pharmacyService.findById(command.getPharmacyId())
                .orElseThrow(() -> new PharmacyNotFoundException(command.getPharmacyId()));

        pharmacy.addStrike();
        Pharmacy updated = pharmacyService.update(pharmacy);

        log.info("Strike added to pharmacy {}: strikeCount={}",
                updated.getId(), updated.getStrikeCount());
        return updated;
    }
}
