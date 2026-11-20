package com.medicinelocator.salesservice.application.service;

import com.medicinelocator.salesservice.application.dto.request.PharmacyCustomerRequest;
import com.medicinelocator.salesservice.domain.exception.PharmacyCustomerNotFoundException;
import com.medicinelocator.salesservice.domain.model.PharmacyCustomer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * CRUD for a pharmacy's own walk-in customers, scoped to the owning pharmacy.
 */
@Service
public class PharmacyCustomerService {

    private static final Logger log = LoggerFactory.getLogger(PharmacyCustomerService.class);

    private final PharmacyCustomerRepository customerRepository;

    public PharmacyCustomerService(PharmacyCustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public PharmacyCustomer create(UUID pharmacyId, PharmacyCustomerRequest request) {
        PharmacyCustomer customer = new PharmacyCustomer(
                UUID.randomUUID(),
                pharmacyId,
                trim(request.getName()),
                trim(request.getPhone()),
                trim(request.getEmail()),
                trim(request.getNote()),
                LocalDateTime.now());
        PharmacyCustomer saved = customerRepository.save(customer);
        log.info("Pharmacy customer created: id={} pharmacyId={}", saved.getId(), pharmacyId);
        return saved;
    }

    @Transactional(readOnly = true)
    public PharmacyCustomer getById(UUID pharmacyId, UUID customerId) {
        return customerRepository.findByIdAndPharmacyId(customerId, pharmacyId)
                .orElseThrow(() -> new PharmacyCustomerNotFoundException(pharmacyId, customerId));
    }

    @Transactional(readOnly = true)
    public List<PharmacyCustomer> list(UUID pharmacyId) {
        return customerRepository.findByPharmacyId(pharmacyId);
    }

    @Transactional
    public PharmacyCustomer update(UUID pharmacyId, UUID customerId, PharmacyCustomerRequest request) {
        PharmacyCustomer customer = customerRepository.findByIdAndPharmacyId(customerId, pharmacyId)
                .orElseThrow(() -> new PharmacyCustomerNotFoundException(pharmacyId, customerId));
        customer.updateDetails(
                trim(request.getName()),
                trim(request.getPhone()),
                trim(request.getEmail()),
                trim(request.getNote()));
        PharmacyCustomer updated = customerRepository.update(customer);
        log.info("Pharmacy customer updated: id={} pharmacyId={}", customerId, pharmacyId);
        return updated;
    }

    @Transactional
    public void delete(UUID pharmacyId, UUID customerId) {
        PharmacyCustomer customer = customerRepository.findByIdAndPharmacyId(customerId, pharmacyId)
                .orElseThrow(() -> new PharmacyCustomerNotFoundException(pharmacyId, customerId));
        customerRepository.deleteById(customer.getId());
        log.info("Pharmacy customer deleted: id={} pharmacyId={}", customerId, pharmacyId);
    }

    private String trim(String value) {
        return value != null ? value.trim() : null;
    }
}
