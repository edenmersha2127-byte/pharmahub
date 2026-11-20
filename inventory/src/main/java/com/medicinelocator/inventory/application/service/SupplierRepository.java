package com.medicinelocator.inventory.application.service;

import com.medicinelocator.inventory.domain.model.Supplier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for suppliers.
 */
public interface SupplierRepository {

    Supplier save(Supplier supplier);

    Supplier update(Supplier supplier);

    Optional<Supplier> findByIdAndPharmacyId(UUID supplierId, UUID pharmacyId);

    List<Supplier> findByPharmacyId(UUID pharmacyId);

    void deleteById(UUID supplierId);
}
