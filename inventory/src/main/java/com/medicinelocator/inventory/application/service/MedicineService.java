package com.medicinelocator.inventory.application.service;

import com.medicinelocator.inventory.domain.model.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicineService {

    Medicine save(Medicine medicine);

    Optional<Medicine> findByIdAndPharmacyId(UUID medicineId, UUID pharmacyId);

    Optional<Medicine> findByBarcode(UUID pharmacyId, String barcode);

    List<Medicine> searchByName(UUID pharmacyId, String query);

    boolean existsByPharmacyIdAndMedicineName(UUID pharmacyId, String medicineName);

    Medicine update(Medicine medicine);

    void deleteById(UUID medicineId);

    Page<Medicine> findByPharmacyId(UUID pharmacyId, boolean availableOnly, Pageable pageable);

    Page<Medicine> searchAcrossPharmacies(String medicineName, String brandName,
                                          String genericName, String category,
                                          Boolean availableOnly, Boolean requiresPrescription,
                                          Pageable pageable);

    long countActiveMedicines(UUID pharmacyId);

    /** Count of active medicines that currently have stock on hand (stockQuantity &gt; 0). */
    long countInStock(UUID pharmacyId);

    /** Total units on hand across all active medicines. */
    long sumStockUnits(UUID pharmacyId);

    /** Retail value on hand: Σ price × stockQuantity over active medicines. */
    java.math.BigDecimal sumInventoryValue(UUID pharmacyId);

    /** Distinct pharmacies that own at least one active medicine (for the daily snapshot job). */
    List<UUID> findDistinctActivePharmacyIds();

    List<Medicine> findLowStock(UUID pharmacyId);

    List<Medicine> findExpiringBetween(UUID pharmacyId, LocalDate from, LocalDate to);

    List<Medicine> findExpired(UUID pharmacyId, LocalDate today);
}