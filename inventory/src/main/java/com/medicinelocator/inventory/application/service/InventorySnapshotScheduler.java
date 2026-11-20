package com.medicinelocator.inventory.application.service;

import com.medicinelocator.inventory.domain.model.InventorySnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Records a daily catalogue-size + inventory-value snapshot per pharmacy so the
 * dashboard can show month-over-month trend deltas. Snapshots accrue going
 * forward (no historical backfill), so trend chips stay empty until the job has
 * run for ~30 days. Idempotent: at most one snapshot per pharmacy per day.
 */
@Component
public class InventorySnapshotScheduler {

    private static final Logger log = LoggerFactory.getLogger(InventorySnapshotScheduler.class);

    private final MedicineService medicineService;
    private final InventorySnapshotRepository snapshotRepository;

    public InventorySnapshotScheduler(MedicineService medicineService,
                                      InventorySnapshotRepository snapshotRepository) {
        this.medicineService = medicineService;
        this.snapshotRepository = snapshotRepository;
    }

    /** Runs daily at 00:30 by default (override with inventory.snapshot-cron). */
    @Scheduled(cron = "${inventory.snapshot-cron:0 30 0 * * *}")
    public void captureDailySnapshots() {
        try {
            LocalDate today = LocalDate.now();
            List<UUID> pharmacyIds = medicineService.findDistinctActivePharmacyIds();
            int written = 0;
            for (UUID pharmacyId : pharmacyIds) {
                if (snapshotRepository.existsForDate(pharmacyId, today)) {
                    continue;
                }
                long medicineCount = medicineService.countActiveMedicines(pharmacyId);
                BigDecimal inventoryValue = medicineService.sumInventoryValue(pharmacyId);
                snapshotRepository.save(new InventorySnapshot(
                        null, pharmacyId, today, medicineCount, inventoryValue, null));
                written++;
            }
            log.info("Inventory snapshot complete: {} pharmacies, {} snapshots written for {}",
                    pharmacyIds.size(), written, today);
        } catch (Exception e) {
            log.error("Inventory snapshot job failed: {}", e.getMessage(), e);
        }
    }
}
