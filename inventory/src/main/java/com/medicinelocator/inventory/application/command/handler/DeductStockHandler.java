package com.medicinelocator.inventory.application.command.handler;

import com.medicinelocator.inventory.application.command.DeductStockCommand;
import com.medicinelocator.inventory.application.dto.response.PharmacyProfileResponse;
import com.medicinelocator.inventory.application.service.BatchStockService;
import com.medicinelocator.inventory.application.service.MedicineService;
import com.medicinelocator.inventory.application.service.PharmacyProfileService;
import com.medicinelocator.inventory.application.service.SearchIndexClient;
import com.medicinelocator.inventory.application.service.StockMovementRecorder;
import com.medicinelocator.inventory.application.service.StockNotificationPublisher;
import com.medicinelocator.inventory.domain.enums.StockMovementType;
import com.medicinelocator.inventory.domain.exception.MedicineNotFoundException;
import com.medicinelocator.inventory.domain.model.Medicine;
import com.medicinelocator.inventory.infrastructure.redis.InventoryCacheService;
import com.medicinelocator.inventory.infrastructure.security.CurrentUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class DeductStockHandler {

    private static final Logger log = LoggerFactory.getLogger(DeductStockHandler.class);

    private final MedicineService medicineService;
    private final PharmacyProfileService pharmacyProfileService;
    private final SearchIndexClient searchIndexClient;
    private final InventoryCacheService cacheService;
    private final StockNotificationPublisher stockNotificationPublisher;
    private final BatchStockService batchStockService;
    private final StockMovementRecorder stockMovementRecorder;
    private final CurrentUserProvider currentUserProvider;

    public DeductStockHandler(MedicineService medicineService,
                              PharmacyProfileService pharmacyProfileService,
                              SearchIndexClient searchIndexClient,
                              InventoryCacheService cacheService,
                              StockNotificationPublisher stockNotificationPublisher,
                              BatchStockService batchStockService,
                              StockMovementRecorder stockMovementRecorder,
                              CurrentUserProvider currentUserProvider) {
        this.medicineService = medicineService;
        this.pharmacyProfileService = pharmacyProfileService;
        this.searchIndexClient = searchIndexClient;
        this.cacheService = cacheService;
        this.stockNotificationPublisher = stockNotificationPublisher;
        this.batchStockService = batchStockService;
        this.stockMovementRecorder = stockMovementRecorder;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public Medicine handle(DeductStockCommand command) {
        Medicine medicine = medicineService
                .findByIdAndPharmacyId(command.getMedicineId(), command.getPharmacyId())
                .orElseThrow(() -> new MedicineNotFoundException(
                        command.getPharmacyId(), command.getMedicineId()));

        int oldQty = medicine.getStockQuantity();

        // deductStock validates availability and updates the denormalized total,
        // which remains the authoritative stock figure.
        medicine.deductStock(command.getAmount());

        // Mirror the deduction onto batches FEFO (earliest expiry first). Legacy
        // medicines without batches simply keep the direct decrement above.
        batchStockService.consumeFefo(command.getMedicineId(), command.getAmount());

        Medicine updated = medicineService.update(medicine);
        cacheService.evictPharmacyInventoryCache(command.getPharmacyId());

        stockMovementRecorder.record(updated, StockMovementType.SALE,
                -command.getAmount(), updated.getStockQuantity(),
                "Stock deducted (sale/checkout)", resolveCurrentUserId());

        pushToSearchIndex(updated);
        stockNotificationPublisher.onStockChanged(updated, oldQty, updated.getStockQuantity());

        log.info("Stock deducted and indexed: medicineId={} pharmacyId={} amount={}",
                command.getMedicineId(), command.getPharmacyId(), command.getAmount());
        return updated;
    }

    private UUID resolveCurrentUserId() {
        try {
            return currentUserProvider.getCurrentUser().getUserId();
        } catch (Exception e) {
            return null;
        }
    }

    private void pushToSearchIndex(Medicine medicine) {
        try {
            PharmacyProfileResponse profile = pharmacyProfileService
                    .findById(medicine.getPharmacyId())
                    .orElseThrow(() -> new RuntimeException(
                            "Pharmacy not found in Auth Service for id: "
                                    + medicine.getPharmacyId()));

            searchIndexClient.indexMedicine(
                    medicine,
                    profile.getPharmacyName(),
                    profile.getAddress(),
                    profile.getCity(),
                    profile.getLatitude(),
                    profile.getLongitude(),
                    profile.isOpen()
            );

            log.info("Search index push successful: medicineId={} pharmacyId={}",
                    medicine.getId(), medicine.getPharmacyId());

        } catch (Exception e) {
            log.error("Failed to push medicine to search index: medicineId={} pharmacyId={} error={}",
                    medicine.getId(), medicine.getPharmacyId(), e.getMessage(), e);
        }
    }
}