package com.medicinelocator.inventory.application.command.handler;

import com.medicinelocator.inventory.application.command.RemoveMedicineCommand;
import com.medicinelocator.inventory.application.dto.response.PharmacyProfileResponse;
import com.medicinelocator.inventory.application.service.MedicineService;
import com.medicinelocator.inventory.application.service.PharmacyProfileService;
import com.medicinelocator.inventory.application.service.SearchIndexClient;
import com.medicinelocator.inventory.domain.exception.MedicineNotFoundException;
import com.medicinelocator.inventory.domain.model.Medicine;
import com.medicinelocator.inventory.infrastructure.redis.InventoryCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RemoveMedicineHandler {

    private static final Logger log = LoggerFactory.getLogger(RemoveMedicineHandler.class);

    private final MedicineService medicineService;
    private final PharmacyProfileService pharmacyProfileService;
    private final SearchIndexClient searchIndexClient;
    private final InventoryCacheService cacheService;

    public RemoveMedicineHandler(MedicineService medicineService,
                                 PharmacyProfileService pharmacyProfileService,
                                 SearchIndexClient searchIndexClient,
                                 InventoryCacheService cacheService) {
        this.medicineService = medicineService;
        this.pharmacyProfileService = pharmacyProfileService;
        this.searchIndexClient = searchIndexClient;
        this.cacheService = cacheService;
    }

    @Transactional
    public void handle(RemoveMedicineCommand command) {
        Medicine medicine = medicineService
                .findByIdAndPharmacyId(command.getMedicineId(), command.getPharmacyId())
                .orElseThrow(() -> new MedicineNotFoundException(
                        command.getPharmacyId(), command.getMedicineId()));

        medicine.deactivate();
        Medicine deactivated = medicineService.update(medicine);
        cacheService.evictPharmacyInventoryCache(command.getPharmacyId());

        pushToSearchIndex(deactivated);

        log.info("Medicine deactivated and index updated: medicineId={} pharmacyId={}",
                command.getMedicineId(), command.getPharmacyId());
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
            log.error("Failed to push deactivation to search index: medicineId={} pharmacyId={} error={}",
                    medicine.getId(), medicine.getPharmacyId(), e.getMessage(), e);
        }
    }
}