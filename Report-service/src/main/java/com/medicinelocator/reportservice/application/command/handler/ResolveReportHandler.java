package com.medicinelocator.reportservice.application.command.handler;

import com.medicinelocator.reportservice.application.command.ResolveReportCommand;
import com.medicinelocator.reportservice.application.service.ModerationAuditRecorder;
import com.medicinelocator.reportservice.application.service.NotificationNotifier;
import com.medicinelocator.reportservice.application.service.ReportNotifications;
import com.medicinelocator.reportservice.application.service.ReportRepository;
import com.medicinelocator.reportservice.domain.exception.ReportNotFoundException;
import com.medicinelocator.reportservice.domain.model.Report;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin resolves a report (issue addressed). Notifies the reporter that their
 * report was reviewed.
 */
@Component
public class ResolveReportHandler {

    private static final Logger log = LoggerFactory.getLogger(ResolveReportHandler.class);

    private final ReportRepository reportRepository;
    private final NotificationNotifier notifier;
    private final ModerationAuditRecorder auditRecorder;

    public ResolveReportHandler(ReportRepository reportRepository, NotificationNotifier notifier,
                                ModerationAuditRecorder auditRecorder) {
        this.reportRepository = reportRepository;
        this.notifier = notifier;
        this.auditRecorder = auditRecorder;
    }

    @Transactional
    public Report handle(ResolveReportCommand command) {
        Report report = reportRepository.findById(command.getReportId())
                .orElseThrow(() -> new ReportNotFoundException(command.getReportId()));

        report.resolve(command.getAdminId(), command.getNote());
        Report saved = reportRepository.save(report);

        auditRecorder.recordResolve(command.getAdminId(), command.getActorRole(),
                saved.getId(), saved.getPharmacyId(), command.getNote());

        log.info("Report {} resolved by admin {}", command.getReportId(), command.getAdminId());

        notifier.notify(
                saved.getReporterId(),
                ReportNotifications.CUSTOMER,
                ReportNotifications.COMPLAINT_RESOLVED,
                "Your report was reviewed",
                "Thank you. Your report has been reviewed and resolved by our team.",
                ReportNotifications.LINK_MY_REPORTS);

        return saved;
    }
}
