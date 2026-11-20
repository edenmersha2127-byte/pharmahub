package com.medicinelocator.reportservice.application.command.handler;

import com.medicinelocator.reportservice.application.command.SuspendPharmacyCommand;
import com.medicinelocator.reportservice.application.service.ModerationAuditRecorder;
import com.medicinelocator.reportservice.application.service.NotificationNotifier;
import com.medicinelocator.reportservice.application.service.PharmacyModerationService;
import com.medicinelocator.reportservice.application.service.ReportNotifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Admin temporarily suspends a pharmacy. Suspension state lives in the Auth
 * Service (which also hides the pharmacy from search and auto-lifts it when the
 * window elapses). We apply it there, then notify the pharmacy.
 */
@Component
public class SuspendPharmacyHandler {

    private static final Logger log = LoggerFactory.getLogger(SuspendPharmacyHandler.class);

    private final PharmacyModerationService pharmacyModerationService;
    private final NotificationNotifier notifier;
    private final ModerationAuditRecorder auditRecorder;
    private final int defaultSuspensionDays;

    public SuspendPharmacyHandler(PharmacyModerationService pharmacyModerationService,
                                  NotificationNotifier notifier,
                                  ModerationAuditRecorder auditRecorder,
                                  @Value("${report.default-suspension-days:3}") int defaultSuspensionDays) {
        this.pharmacyModerationService = pharmacyModerationService;
        this.notifier = notifier;
        this.auditRecorder = auditRecorder;
        this.defaultSuspensionDays = defaultSuspensionDays;
    }

    public void handle(SuspendPharmacyCommand command) {
        int days = (command.getDays() != null && command.getDays() > 0)
                ? command.getDays()
                : defaultSuspensionDays;

        // Applies the suspension in Auth; rethrows if that call fails.
        pharmacyModerationService.suspend(command.getPharmacyId(), days);

        auditRecorder.recordSuspend(command.getAdminId(), command.getActorRole(),
                command.getPharmacyId(), days, command.getReason());

        log.info("Pharmacy {} suspended for {} day(s) by admin {}",
                command.getPharmacyId(), days, command.getAdminId());

        String reason = (command.getReason() != null && !command.getReason().isBlank())
                ? " Reason: " + command.getReason()
                : "";
        notifier.notify(
                command.getPharmacyId(),
                ReportNotifications.PHARMACY,
                ReportNotifications.PHARMACY_SUSPENDED,
                "Your pharmacy has been suspended",
                "Your pharmacy has been temporarily suspended for " + days
                        + " day(s) following customer reports." + reason
                        + " During this time it will not appear in customer searches.",
                ReportNotifications.LINK_PHARMACY_REPORTS);
    }
}
