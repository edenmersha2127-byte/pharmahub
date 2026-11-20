package com.medicinelocator.notificationservice.application.command.handler;

import com.medicinelocator.notificationservice.application.command.RemindPharmaciesToUpdateCommand;
import com.medicinelocator.notificationservice.application.dto.response.PharmacySummaryResponse;
import com.medicinelocator.notificationservice.application.service.NotificationService;
import com.medicinelocator.notificationservice.application.service.PharmacyDirectoryService;
import com.medicinelocator.notificationservice.domain.enums.NotificationType;
import com.medicinelocator.notificationservice.domain.enums.RecipientType;
import com.medicinelocator.notificationservice.domain.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Component
public class RemindPharmaciesToUpdateHandler {

    private static final Logger log = LoggerFactory.getLogger(RemindPharmaciesToUpdateHandler.class);

    private static final String APPROVED = "APPROVED";
    private static final String TITLE = "Update your medicines";
    private static final String MESSAGE =
            "Please review and update your medicine stock, prices and availability "
                    + "so customers always see accurate information.";
    private static final String LINK = "/pharmacy/inventory";
    private final PharmacyDirectoryService pharmacyDirectoryService;
    private final NotificationService notificationService;

    public RemindPharmaciesToUpdateHandler(PharmacyDirectoryService pharmacyDirectoryService,
                                           NotificationService notificationService) {
        this.pharmacyDirectoryService = pharmacyDirectoryService;
        this.notificationService = notificationService;
    }

    @Transactional
    public int handle(RemindPharmaciesToUpdateCommand command) {
        List<PharmacySummaryResponse> pharmacies = pharmacyDirectoryService.findAllPharmacies();

        int created = 0;
        for (PharmacySummaryResponse pharmacy : pharmacies) {
            if (!APPROVED.equalsIgnoreCase(pharmacy.getStatus())) {
                continue;
            }
            Notification notification = Notification.create(
                    pharmacy.getPharmacyId(),
                    RecipientType.PHARMACY,
                    NotificationType.MEDICINE_UPDATE_REMINDER,
                    TITLE,
                    MESSAGE,
                    LINK
            );
            notificationService.save(notification);
            created++;
        }

        log.info("Nightly medicine-update reminder sent: pharmaciesTotal={} remindersCreated={}",
                pharmacies.size(), created);
        return created;
    }
}
