package com.medicinelocator.salesservice.application.service;

import com.medicinelocator.salesservice.domain.model.PharmacyCustomer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PharmacyCustomerRepository {

    PharmacyCustomer save(PharmacyCustomer customer);

    PharmacyCustomer update(PharmacyCustomer customer);

    Optional<PharmacyCustomer> findByIdAndPharmacyId(UUID customerId, UUID pharmacyId);

    List<PharmacyCustomer> findByPharmacyId(UUID pharmacyId);

    void deleteById(UUID customerId);
}
