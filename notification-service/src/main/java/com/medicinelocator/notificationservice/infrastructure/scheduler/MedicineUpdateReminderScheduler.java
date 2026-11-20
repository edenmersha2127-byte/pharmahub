package com.medicinelocator.notificationservice.infrastructure.scheduler;

import com.medicinelocator.notificationservice.application.command.RemindPharmaciesToUpdateCommand;
import com.medicinelocator.notificationservice.application.command.handler.RemindPharmaciesToUpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Inbound adapter that fires the nightly "update your medicines" reminder.
 *
 * Schedule and timezone are configured in application.yaml
 * (default 19:00 Africa/Addis_Ababa). Delegates all work to the
 * application command handler — no business logic lives here.
 */
@Component
@ConditionalOnProperty(
        prefix = "notification.medicine-reminder",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class MedicineUpdateReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(MedicineUpdateReminderScheduler.class);

    private final RemindPharmaciesToUpdateHandler remindPharmaciesToUpdateHandler;

    public MedicineUpdateReminderScheduler(
            RemindPharmaciesToUpdateHandler remindPharmaciesToUpdateHandler) {
        this.remindPharmaciesToUpdateHandler = remindPharmaciesToUpdateHandler;
    }

    @Scheduled(
            cron = "${notification.medicine-reminder.cron}",
            zone = "${notification.medicine-reminder.zone}")
    public void sendNightlyMedicineUpdateReminder() {
        log.info("Nightly medicine-update reminder job started");
        try {
            int created = remindPharmaciesToUpdateHandler.handle(
                    new RemindPharmaciesToUpdateCommand());
            log.info("Nightly medicine-update reminder job finished: remindersCreated={}", created);
        } catch (Exception e) {
            log.error("Nightly medicine-update reminder job failed: {}", e.getMessage(), e);
        }
    }
}
