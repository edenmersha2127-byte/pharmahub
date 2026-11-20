package com.medicinelocator.inventory.application.service;

import com.medicinelocator.inventory.domain.model.Medicine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Translates stock-quantity transitions into in-app notifications for the
 * owning pharmacy (LOW_STOCK / OUT_OF_STOCK / MEDICINE_BACK_IN_STOCK).
 *
 * Fires only on threshold *crossings* (not on every change) to avoid spamming
 * the pharmacy. All calls are best-effort — the underlying client never throws.
 */
@Component
public class StockNotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(StockNotificationPublisher.class);
    private static final String RECIPIENT_TYPE = "PHARMACY";
    private static final String LINK = "/pharmacy/inventory";

    private final NotificationClient notificationClient;
    private final int lowStockThreshold;

    public StockNotificationPublisher(
            NotificationClient notificationClient,
            @Value("${inventory.low-stock-threshold:10}") int lowStockThreshold) {
        this.notificationClient = notificationClient;
        this.lowStockThreshold = lowStockThreshold;
    }

    /**
     * Emit a notification when a medicine's stock crosses a meaningful boundary
     * as a result of an update or deduction.
     */
    public void onStockChanged(Medicine medicine, int oldQty, int newQty) {
        if (oldQty == newQty) {
            return;
        }
        String name = medicine.getMedicineName();

        if (newQty == 0 && oldQty > 0) {
            notificationClient.notify(medicine.getPharmacyId(), RECIPIENT_TYPE, "OUT_OF_STOCK",
                    "Out of stock",
                    "'" + name + "' is now out of stock. Restock it so customers see accurate availability.",
                    LINK);
        } else if (oldQty == 0 && newQty > 0) {
            notificationClient.notify(medicine.getPharmacyId(), RECIPIENT_TYPE, "MEDICINE_BACK_IN_STOCK",
                    "Back in stock",
                    "'" + name + "' is back in stock (" + newQty + " available).",
                    LINK);
        } else if (newQty > 0 && newQty < lowStockThreshold && oldQty >= lowStockThreshold) {
            notificationClient.notify(medicine.getPharmacyId(), RECIPIENT_TYPE, "LOW_STOCK",
                    "Low stock",
                    "'" + name + "' is running low (" + newQty + " left). Consider restocking.",
                    LINK);
        }
        log.debug("Stock-change notification evaluated: medicineId={} oldQty={} newQty={} threshold={}",
                medicine.getId(), oldQty, newQty, lowStockThreshold);
    }

    /**
     * Emit an initial-state notification when a medicine is added already at or
     * below the low-stock threshold (or with zero stock).
     */
    public void onMedicineAdded(Medicine medicine, int qty) {
        String name = medicine.getMedicineName();
        if (qty == 0) {
            notificationClient.notify(medicine.getPharmacyId(), RECIPIENT_TYPE, "OUT_OF_STOCK",
                    "Out of stock",
                    "'" + name + "' was added with no stock. Add quantity so customers can find it.",
                    LINK);
        } else if (qty < lowStockThreshold) {
            notificationClient.notify(medicine.getPharmacyId(), RECIPIENT_TYPE, "LOW_STOCK",
                    "Low stock",
                    "'" + name + "' was added with low stock (" + qty + " units).",
                    LINK);
        }
    }
}
