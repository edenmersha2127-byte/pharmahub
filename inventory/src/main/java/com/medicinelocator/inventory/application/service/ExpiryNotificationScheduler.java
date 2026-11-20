package com.medicinelocator.inventory.application.service;

import com.medicinelocator.inventory.domain.model.MedicineBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Scheduled scan that warns each pharmacy about batches expiring soon. Runs
 * daily (configurable) and sends one MEDICINE_EXPIRING notification per
 * affected pharmacy. Best-effort: a failure never propagates.
 */
@Component
public class ExpiryNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExpiryNotificationScheduler.class);
    private static final String RECIPIENT_TYPE = "PHARMACY";
    private static final String INVENTORY_LINK = "/pharmacy/inventory";

    private final MedicineBatchRepository batchRepository;
    private final NotificationClient notificationClient;
    private final int warningDays;

    public ExpiryNotificationScheduler(MedicineBatchRepository batchRepository,
                                       NotificationClient notificationClient,
                                       @Value("${inventory.expiry-warning-days:30}") int warningDays) {
        this.batchRepository = batchRepository;
        this.notificationClient = notificationClient;
        this.warningDays = warningDays;
    }

    /** Runs daily at 06:00 by default (override with inventory.expiry-scan-cron). */
    @Scheduled(cron = "${inventory.expiry-scan-cron:0 0 6 * * *}")
    public void scanForExpiringBatches() {
        try {
            LocalDate today = LocalDate.now();
            List<MedicineBatch> expiring =
                    batchRepository.findExpiringBetweenAllPharmacies(today, today.plusDays(warningDays));
            if (expiring.isEmpty()) {
                log.debug("Expiry scan: no batches expiring within {} days", warningDays);
                return;
            }

            Map<UUID, List<MedicineBatch>> byPharmacy = expiring.stream()
                    .collect(Collectors.groupingBy(MedicineBatch::getPharmacyId));

            for (Map.Entry<UUID, List<MedicineBatch>> entry : byPharmacy.entrySet()) {
                UUID pharmacyId = entry.getKey();
                int batchCount = entry.getValue().size();
                notificationClient.notify(pharmacyId, RECIPIENT_TYPE, "MEDICINE_EXPIRING",
                        "Medicines expiring soon",
                        batchCount + " batch(es) expire within the next " + warningDays
                                + " days. Review them to avoid dead stock.",
                        INVENTORY_LINK);
            }

            log.info("Expiry scan complete: {} pharmacies notified, {} batches expiring within {} days",
                    byPharmacy.size(), expiring.size(), warningDays);
        } catch (Exception e) {
            log.error("Expiry scan failed: {}", e.getMessage(), e);
        }
    }
}
