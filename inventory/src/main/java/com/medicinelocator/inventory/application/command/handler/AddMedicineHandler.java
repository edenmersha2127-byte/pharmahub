package com.medicinelocator.inventory.application.command.handler;

import com.medicinelocator.inventory.application.command.AddMedicineCommand;
import com.medicinelocator.inventory.application.dto.response.PharmacyProfileResponse;
import com.medicinelocator.inventory.application.service.MedicineService;
import com.medicinelocator.inventory.application.service.PharmacyProfileService;
import com.medicinelocator.inventory.application.service.SearchIndexClient;
import com.medicinelocator.inventory.application.service.StockMovementRecorder;
import com.medicinelocator.inventory.application.service.StockNotificationPublisher;
import com.medicinelocator.inventory.domain.enums.StockMovementType;
import com.medicinelocator.inventory.domain.exception.DuplicateMedicineException;
import com.medicinelocator.inventory.domain.model.Medicine;
import com.medicinelocator.inventory.infrastructure.redis.InventoryCacheService;
import com.medicinelocator.inventory.infrastructure.security.CurrentUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AddMedicineHandler {

    private static final Logger log = LoggerFactory.getLogger(AddMedicineHandler.class);

    private final MedicineService medicineService;
    private final PharmacyProfileService pharmacyProfileService;
    private final SearchIndexClient searchIndexClient;
    private final InventoryCacheService cacheService;
    private final StockNotificationPublisher stockNotificationPublisher;
    private final StockMovementRecorder stockMovementRecorder;
    private final CurrentUserProvider currentUserProvider;

    public AddMedicineHandler(MedicineService medicineService,
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
    public Medicine handle(AddMedicineCommand command) {
        if (medicineService.existsByPharmacyIdAndMedicineName(
                command.getPharmacyId(), command.getMedicineName())) {
            throw new DuplicateMedicineException(
                    "Medicine '" + command.getMedicineName()
                            + "' already exists in your inventory");
        }

        boolean available = command.getStockQuantity() > 0;

        Medicine medicine = new Medicine(
                UUID.randomUUID(),
                command.getPharmacyId(),
                command.getMedicineName(),
                command.getGenericName(),
                command.getBrandName(),
                command.getCategory(),
                command.getDescription(),
                command.getPrice(),
                command.getStockQuantity(),
                available,
                command.isRequiresPrescription(),
                command.getExpiryDate(),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        medicine.setPurchasePrice(command.getPurchasePrice());
        medicine.setMinimumStockLevel(command.getMinimumStockLevel());
        medicine.setSupplierId(command.getSupplierId());
        medicine.setBarcode(command.getBarcode());
        medicine.setManufacturer(command.getManufacturer());

        Medicine saved = medicineService.save(medicine);
        cacheService.evictPharmacyInventoryCache(command.getPharmacyId());

        stockMovementRecorder.record(saved, StockMovementType.INITIAL,
                saved.getStockQuantity(), saved.getStockQuantity(),
                "Medicine added to inventory", resolveCurrentUserId());

        pushToSearchIndex(saved);
        stockNotificationPublisher.onMedicineAdded(saved, saved.getStockQuantity());

        log.info("Medicine added: id={} pharmacyId={} name={}",
                saved.getId(), saved.getPharmacyId(), saved.getMedicineName());
        return saved;
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