package com.medicinelocator.inventory.application.command.handler;

import com.medicinelocator.inventory.application.command.UpdateStockCommand;
import com.medicinelocator.inventory.application.dto.response.PharmacyProfileResponse;
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
public class UpdateStockHandler {

    private static final Logger log = LoggerFactory.getLogger(UpdateStockHandler.class);

    private final MedicineService medicineService;
    private final PharmacyProfileService pharmacyProfileService;
    private final SearchIndexClient searchIndexClient;
    private final InventoryCacheService cacheService;
    private final StockNotificationPublisher stockNotificationPublisher;
    private final StockMovementRecorder stockMovementRecorder;
    private final CurrentUserProvider currentUserProvider;

    public UpdateStockHandler(MedicineService medicineService,
                              PharmacyProfileService pharmacyProfileService,
                              SearchIndexClient searchIndexClient,
                              InventoryCacheService cacheService,
                              StockNotificationPublisher stockNotificationPublisher,
                              StockMovementRecorder stockMovementRecorder,
                              CurrentUserProvider currentUserProvider) {
        this.medicineService = medicineService;
        this.pharmacyProfileService = pharmacyProfileService;
        this.searchIndexClient = searchIndexClient;
        this.cacheService = cacheService;
        this.stockNotificationPublisher = stockNotificationPublisher;
        this.stockMovementRecorder = stockMovementRecorder;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public Medicine handle(UpdateStockCommand command) {
        Medicine medicine = medicineService
                .findByIdAndPharmacyId(command.getMedicineId(), command.getPharmacyId())
                .orElseThrow(() -> new MedicineNotFoundException(
                        command.getPharmacyId(), command.getMedicineId()));

        int oldQty = medicine.getStockQuantity();
        medicine.updateDetails(
                command.getMedicineName(),
                command.getGenericName(),
                command.getBrandName(),
                command.getCategory(),
                command.getDescription(),
                command.getPrice(),
                command.getStockQuantity(),
                command.isRequiresPrescription(),
                command.getExpiryDate()
        );
        medicine.setPurchasePrice(command.getPurchasePrice());
        medicine.setMinimumStockLevel(command.getMinimumStockLevel());
        medicine.setSupplierId(command.getSupplierId());
        medicine.setBarcode(command.getBarcode());
        medicine.setManufacturer(command.getManufacturer());

        Medicine updated = medicineService.update(medicine);
        cacheService.evictPharmacyInventoryCache(command.getPharmacyId());

        int delta = updated.getStockQuantity() - oldQty;
        if (delta != 0) {
            stockMovementRecorder.record(updated, StockMovementType.ADJUSTMENT,
                    delta, updated.getStockQuantity(),
                    "Stock adjusted via medicine update", resolveCurrentUserId());
        }

        pushToSearchIndex(updated);
        stockNotificationPublisher.onStockChanged(updated, oldQty, updated.getStockQuantity());

        log.info("Medicine updated: id={} pharmacyId={}", updated.getId(), updated.getPharmacyId());
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