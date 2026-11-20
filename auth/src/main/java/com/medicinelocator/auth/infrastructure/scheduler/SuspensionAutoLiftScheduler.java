package com.medicinelocator.auth.infrastructure.scheduler;

import com.medicinelocator.auth.application.command.LiftPharmacySuspensionCommand;
import com.medicinelocator.auth.application.command.handler.LiftPharmacySuspensionHandler;
import com.medicinelocator.auth.application.service.PharmacyService;
import com.medicinelocator.auth.domain.model.Pharmacy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Periodically reactivates pharmacies whose temporary suspension window has
 * elapsed. Runs hourly by default (override auth.suspension.auto-lift-cron).
 */
@Component
@ConditionalOnProperty(
        prefix = "auth.suspension",
        name = "auto-lift-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class SuspensionAutoLiftScheduler {

    private static final Logger log = LoggerFactory.getLogger(SuspensionAutoLiftScheduler.class);

    private final PharmacyService pharmacyService;
    private final LiftPharmacySuspensionHandler liftPharmacySuspensionHandler;

    public SuspensionAutoLiftScheduler(PharmacyService pharmacyService,
                                       LiftPharmacySuspensionHandler liftPharmacySuspensionHandler) {
        this.pharmacyService = pharmacyService;
        this.liftPharmacySuspensionHandler = liftPharmacySuspensionHandler;
    }

    @Scheduled(cron = "${auth.suspension.auto-lift-cron:0 0 * * * *}")
    public void liftExpiredSuspensions() {
        List<Pharmacy> expired = pharmacyService.findExpiredSuspensions(LocalDateTime.now());
        if (expired.isEmpty()) {
            return;
        }
        log.info("Auto-lift job: {} expired suspension(s) to lift", expired.size());
        for (Pharmacy pharmacy : expired) {
            try {
                liftPharmacySuspensionHandler.handle(new LiftPharmacySuspensionCommand(pharmacy.getId()));
            } catch (Exception e) {
                log.error("Failed to auto-lift suspension for pharmacy {}: {}",
                        pharmacy.getId(), e.getMessage(), e);
            }
        }
    }
}
