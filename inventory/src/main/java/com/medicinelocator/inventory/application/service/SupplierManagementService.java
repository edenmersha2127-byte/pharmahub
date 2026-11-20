package com.medicinelocator.inventory.application.service;

import com.medicinelocator.inventory.application.dto.request.SupplierRequest;
import com.medicinelocator.inventory.domain.exception.SupplierNotFoundException;
import com.medicinelocator.inventory.domain.model.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application service for supplier CRUD, scoped to the owning pharmacy.
 */
@Service
public class SupplierManagementService {

    private static final Logger log = LoggerFactory.getLogger(SupplierManagementService.class);

    private final SupplierRepository supplierRepository;

    public SupplierManagementService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Transactional
    public Supplier create(UUID pharmacyId, SupplierRequest request) {
        Supplier supplier = new Supplier(
                UUID.randomUUID(),
                pharmacyId,
                trim(request.getName()),
                trim(request.getPhone()),
                trim(request.getEmail()),
                trim(request.getAddress()),
                trim(request.getNote()),
                LocalDateTime.now()
        );
        Supplier saved = supplierRepository.save(supplier);
        log.info("Supplier created: id={} pharmacyId={}", saved.getId(), pharmacyId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Supplier getById(UUID pharmacyId, UUID supplierId) {
        return supplierRepository.findByIdAndPharmacyId(supplierId, pharmacyId)
                .orElseThrow(() -> new SupplierNotFoundException(pharmacyId, supplierId));
    }

    @Transactional(readOnly = true)
    public List<Supplier> list(UUID pharmacyId) {
        return supplierRepository.findByPharmacyId(pharmacyId);
    }

    @Transactional
    public Supplier update(UUID pharmacyId, UUID supplierId, SupplierRequest request) {
        Supplier supplier = supplierRepository.findByIdAndPharmacyId(supplierId, pharmacyId)
                .orElseThrow(() -> new SupplierNotFoundException(pharmacyId, supplierId));
        supplier.updateDetails(
                trim(request.getName()),
                trim(request.getPhone()),
                trim(request.getEmail()),
                trim(request.getAddress()),
                trim(request.getNote())
        );
        Supplier updated = supplierRepository.update(supplier);
        log.info("Supplier updated: id={} pharmacyId={}", supplierId, pharmacyId);
        return updated;
    }

    @Transactional
    public void delete(UUID pharmacyId, UUID supplierId) {
        Supplier supplier = supplierRepository.findByIdAndPharmacyId(supplierId, pharmacyId)
                .orElseThrow(() -> new SupplierNotFoundException(pharmacyId, supplierId));
        supplierRepository.deleteById(supplier.getId());
        log.info("Supplier deleted: id={} pharmacyId={}", supplierId, pharmacyId);
    }

    private String trim(String value) {
        return value != null ? value.trim() : null;
    }
}
