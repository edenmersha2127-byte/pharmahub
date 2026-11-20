package com.medicinelocator.reportservice.application.command.handler;

import com.medicinelocator.reportservice.application.command.DismissReportCommand;
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
 * Admin dismisses a report as unfounded. Notifies the reporter it was reviewed.
 */
@Component
public class DismissReportHandler {

    private static final Logger log = LoggerFactory.getLogger(DismissReportHandler.class);

    private final ReportRepository reportRepository;
    private final NotificationNotifier notifier;
    private final ModerationAuditRecorder auditRecorder;

    public DismissReportHandler(ReportRepository reportRepository, NotificationNotifier notifier,
                                ModerationAuditRecorder auditRecorder) {
        this.reportRepository = reportRepository;
        this.notifier = notifier;
        this.auditRecorder = auditRecorder;
    }

    @Transactional
    public Report handle(DismissReportCommand command) {
        Report report = reportRepository.findById(command.getReportId())
                .orElseThrow(() -> new ReportNotFoundException(command.getReportId()));

        report.dismiss(command.getAdminId(), command.getNote());
        Report saved = reportRepository.save(report);

        auditRecorder.recordDismiss(command.getAdminId(), command.getActorRole(),
                saved.getId(), saved.getPharmacyId(), command.getNote());

        log.info("Report {} dismissed by admin {}", command.getReportId(), command.getAdminId());

        notifier.notify(
                saved.getReporterId(),
                ReportNotifications.CUSTOMER,
                ReportNotifications.COMPLAINT_RESOLVED,
                "Your report was reviewed",
                "Your report has been reviewed. No further action was needed after our assessment.",
                ReportNotifications.LINK_MY_REPORTS);

        return saved;
    }
}
