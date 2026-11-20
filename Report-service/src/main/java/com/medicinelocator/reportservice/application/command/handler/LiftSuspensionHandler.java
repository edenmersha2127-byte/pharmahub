package com.medicinelocator.reportservice.application.command.handler;

import com.medicinelocator.reportservice.application.command.LiftSuspensionCommand;
import com.medicinelocator.reportservice.application.service.ModerationAuditRecorder;
import com.medicinelocator.reportservice.application.service.NotificationNotifier;
import com.medicinelocator.reportservice.application.service.PharmacyModerationService;
import com.medicinelocator.reportservice.application.service.ReportNotifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Admin lifts an active suspension early. Delegates the state change to Auth
 * and notifies the pharmacy that it is active again.
 */
@Component
public class LiftSuspensionHandler {

    private static final Logger log = LoggerFactory.getLogger(LiftSuspensionHandler.class);

    private final PharmacyModerationService pharmacyModerationService;
    private final NotificationNotifier notifier;
    private final ModerationAuditRecorder auditRecorder;

    public LiftSuspensionHandler(PharmacyModerationService pharmacyModerationService,
                                 NotificationNotifier notifier,
                                 ModerationAuditRecorder auditRecorder) {
        this.pharmacyModerationService = pharmacyModerationService;
        this.notifier = notifier;
        this.auditRecorder = auditRecorder;
    }

    public void handle(LiftSuspensionCommand command) {
        // Lifts the suspension in Auth; rethrows if that call fails.
        pharmacyModerationService.liftSuspension(command.getPharmacyId());

        auditRecorder.recordLift(command.getAdminId(), command.getActorRole(),
                command.getPharmacyId());

        log.info("Suspension lifted for pharmacy {} by admin {}",
                command.getPharmacyId(), command.getAdminId());

        notifier.notify(
                command.getPharmacyId(),
                ReportNotifications.PHARMACY,
                ReportNotifications.GENERAL,
                "Your suspension has been lifted",
                "Your pharmacy's suspension has been lifted and your account is active again."
                        + " Please keep your status and availability up to date.",
                ReportNotifications.LINK_PHARMACY_REPORTS);
    }
}
