package com.medicinelocator.reportservice.application.command.handler;

import com.medicinelocator.reportservice.application.command.WarnPharmacyCommand;
import com.medicinelocator.reportservice.application.service.ModerationAuditRecorder;
import com.medicinelocator.reportservice.application.service.NotificationNotifier;
import com.medicinelocator.reportservice.application.service.PharmacyModerationService;
import com.medicinelocator.reportservice.application.service.ReportNotifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Admin issues a formal warning (strike) to a pharmacy. The strike counter
 * lives in the Auth Service, so we call it and then notify the pharmacy.
 */
@Component
public class WarnPharmacyHandler {

    private static final Logger log = LoggerFactory.getLogger(WarnPharmacyHandler.class);

    private final PharmacyModerationService pharmacyModerationService;
    private final NotificationNotifier notifier;
    private final ModerationAuditRecorder auditRecorder;

    public WarnPharmacyHandler(PharmacyModerationService pharmacyModerationService,
                               NotificationNotifier notifier,
                               ModerationAuditRecorder auditRecorder) {
        this.pharmacyModerationService = pharmacyModerationService;
        this.notifier = notifier;
        this.auditRecorder = auditRecorder;
    }

    public void handle(WarnPharmacyCommand command) {
        // Applies the strike in Auth; rethrows if that call fails.
        pharmacyModerationService.addStrike(command.getPharmacyId());

        auditRecorder.recordWarn(command.getAdminId(), command.getActorRole(),
                command.getPharmacyId(), command.getReason());

        log.info("Pharmacy {} warned by admin {}", command.getPharmacyId(), command.getAdminId());

        String reason = (command.getReason() != null && !command.getReason().isBlank())
                ? " Reason: " + command.getReason()
                : "";
        notifier.notify(
                command.getPharmacyId(),
                ReportNotifications.PHARMACY,
                ReportNotifications.PHARMACY_WARNED,
                "You have received a warning",
                "Our team has issued a formal warning to your pharmacy following customer reports."
                        + reason + " Repeated issues may lead to temporary suspension.",
                ReportNotifications.LINK_PHARMACY_REPORTS);
    }
}
