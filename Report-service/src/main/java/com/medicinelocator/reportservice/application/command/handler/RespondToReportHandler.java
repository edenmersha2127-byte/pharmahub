package com.medicinelocator.reportservice.application.command.handler;

import com.medicinelocator.reportservice.application.command.RespondToReportCommand;
import com.medicinelocator.reportservice.application.dto.response.PharmacySummaryResponse;
import com.medicinelocator.reportservice.application.service.NotificationNotifier;
import com.medicinelocator.reportservice.application.service.PharmacyModerationService;
import com.medicinelocator.reportservice.application.service.ReportNotifications;
import com.medicinelocator.reportservice.application.service.ReportRepository;
import com.medicinelocator.reportservice.domain.exception.ReportNotFoundException;
import com.medicinelocator.reportservice.domain.model.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Pharmacy right-to-respond: records the pharmacy's reply, moves the report into
 * review, and alerts the report admins so they can weigh both sides.
 */
@Component
public class RespondToReportHandler {

    private static final Logger log = LoggerFactory.getLogger(RespondToReportHandler.class);

    private final ReportRepository reportRepository;
    private final NotificationNotifier notifier;
    private final PharmacyModerationService pharmacyModerationService;

    public RespondToReportHandler(ReportRepository reportRepository,
                                  NotificationNotifier notifier,
                                  PharmacyModerationService pharmacyModerationService) {
        this.reportRepository = reportRepository;
        this.notifier = notifier;
        this.pharmacyModerationService = pharmacyModerationService;
    }

    @Transactional
    public Report handle(RespondToReportCommand command) {
        Report report = reportRepository.findById(command.getReportId())
                .orElseThrow(() -> new ReportNotFoundException(command.getReportId()));

        report.respondAsPharmacy(command.getPharmacyId(), command.getResponse());
        Report saved = reportRepository.save(report);

        log.info("Pharmacy {} responded to report {}", command.getPharmacyId(), command.getReportId());

        notifyReportAdmins(saved);
        return saved;
    }

    private void notifyReportAdmins(Report report) {
        List<UUID> adminIds = pharmacyModerationService.getReportAdminIds();
        if (adminIds.isEmpty()) {
            return;
        }
        String pharmacyName = pharmacyModerationService.getPharmacy(report.getPharmacyId())
                .map(PharmacySummaryResponse::getPharmacyName)
                .orElse("A pharmacy");

        for (UUID adminId : adminIds) {
            notifier.notify(
                    adminId,
                    ReportNotifications.ADMIN,
                    ReportNotifications.PHARMACY_RESPONDED,
                    "Pharmacy responded to a report",
                    "'" + pharmacyName + "' has responded to a report. It is ready for review.",
                    ReportNotifications.LINK_ADMIN_REPORTS);
        }
    }
}
