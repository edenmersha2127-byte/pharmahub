package com.medicinelocator.reportservice.application.command.handler;

import com.medicinelocator.reportservice.application.command.SubmitReportCommand;
import com.medicinelocator.reportservice.application.dto.response.PharmacySummaryResponse;
import com.medicinelocator.reportservice.application.service.NotificationNotifier;
import com.medicinelocator.reportservice.application.service.PharmacyModerationService;
import com.medicinelocator.reportservice.application.service.ReportNotifications;
import com.medicinelocator.reportservice.application.service.ReportRepository;
import com.medicinelocator.reportservice.domain.enums.ReportCategory;
import com.medicinelocator.reportservice.domain.exception.DuplicateReportException;
import com.medicinelocator.reportservice.domain.model.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Files a report and drives the automatic (system) side of the hybrid
 * enforcement model:
 *   1. anti-spam cooldown guard
 *   2. persist the report
 *   3. auto-alert the pharmacy (a nudge, not a punishment)
 *   4. alert every report admin about the new report (so nothing is missed)
 *   5. if unresolved reports cross the threshold, escalate: flag the pharmacy
 *
 * Punishments (warn/suspend) are never applied here — only an admin does those.
 */
@Component
public class SubmitReportHandler {

    private static final Logger log = LoggerFactory.getLogger(SubmitReportHandler.class);

    private final ReportRepository reportRepository;
    private final NotificationNotifier notifier;
    private final PharmacyModerationService pharmacyModerationService;

    private final long cooldownHours;
    private final long flagThreshold;
    private final long flagWindowDays;

    public SubmitReportHandler(ReportRepository reportRepository,
                               NotificationNotifier notifier,
                               PharmacyModerationService pharmacyModerationService,
                               @Value("${report.cooldown-hours:24}") long cooldownHours,
                               @Value("${report.flag-threshold:3}") long flagThreshold,
                               @Value("${report.flag-window-days:7}") long flagWindowDays) {
        this.reportRepository = reportRepository;
        this.notifier = notifier;
        this.pharmacyModerationService = pharmacyModerationService;
        this.cooldownHours = cooldownHours;
        this.flagThreshold = flagThreshold;
        this.flagWindowDays = flagWindowDays;
    }

    @Transactional
    public Report handle(SubmitReportCommand command) {
        LocalDateTime now = LocalDateTime.now();

        // 1. anti-spam: one report per customer+pharmacy per cooldown window
        if (reportRepository.existsRecentReport(
                command.getReporterId(), command.getPharmacyId(), now.minusHours(cooldownHours))) {
            throw new DuplicateReportException(
                    "You have already reported this pharmacy recently. Please wait before reporting again.");
        }

        // 2. persist
        Report saved = reportRepository.save(Report.create(
                command.getReporterId(),
                command.getPharmacyId(),
                command.getCategory(),
                command.getDescription()));

        log.info("Report filed: id={} pharmacyId={} category={}",
                saved.getId(), saved.getPharmacyId(), saved.getCategory());

        // 3. system auto-alert to the pharmacy (bell notification)
        alertPharmacy(saved);

        // 4. alert every report admin about this new report + 5. escalate if needed
        notifyReportAdmins(saved, now);

        return saved;
    }

    private void alertPharmacy(Report report) {
        String action = switch (report.getCategory()) {
            case PHARMACY_CLOSED -> " Please make sure your open/closed status is up to date.";
            case MEDICINE_UNAVAILABLE -> " Please make sure your medicine availability is up to date.";
            default -> "";
        };
        notifier.notify(
                report.getPharmacyId(),
                ReportNotifications.PHARMACY,
                ReportNotifications.COMPLAINT_RECEIVED,
                "A customer reported an issue",
                "A customer reported an issue (" + humanize(report.getCategory()) + ")." + action,
                ReportNotifications.LINK_PHARMACY_REPORTS);
    }

    /**
     * Notify report admins about the new report, and — if the pharmacy has now
     * crossed the unresolved-report threshold — send a stronger escalation ping.
     * Admin ids and the pharmacy name are fetched once and reused.
     */
    private void notifyReportAdmins(Report report, LocalDateTime now) {
        List<UUID> adminIds = pharmacyModerationService.getReportAdminIds();
        if (adminIds.isEmpty()) {
            log.info("No report admins to notify for report {} (pharmacy {})",
                    report.getId(), report.getPharmacyId());
            return;
        }

        String pharmacyName = pharmacyModerationService.getPharmacy(report.getPharmacyId())
                .map(PharmacySummaryResponse::getPharmacyName)
                .orElse("a pharmacy");

        // 4. per-report alert to every report admin
        for (UUID adminId : adminIds) {
            notifier.notify(
                    adminId,
                    ReportNotifications.ADMIN,
                    ReportNotifications.NEW_REPORT_SUBMITTED,
                    "New report filed",
                    "'" + pharmacyName + "' was reported by a customer ("
                            + humanize(report.getCategory()) + ").",
                    ReportNotifications.LINK_ADMIN_REPORTS);
        }

        // 5. escalation: this pharmacy is now a repeat offender
        long unresolved = reportRepository.countUnresolvedForPharmacySince(
                report.getPharmacyId(), now.minusDays(flagWindowDays));
        if (unresolved < flagThreshold) {
            return;
        }
        log.info("Pharmacy {} flagged for review: {} unresolved reports; escalating to {} report admin(s)",
                report.getPharmacyId(), unresolved, adminIds.size());
        for (UUID adminId : adminIds) {
            notifier.notify(
                    adminId,
                    ReportNotifications.ADMIN,
                    ReportNotifications.NEW_REPORT_FLAGGED,
                    "Pharmacy flagged for review",
                    "'" + pharmacyName + "' has " + unresolved
                            + " unresolved reports and needs review.",
                    ReportNotifications.LINK_ADMIN_REPORTS);
        }
    }

    private String humanize(ReportCategory category) {
        return category.name().toLowerCase().replace('_', ' ');
    }
}
