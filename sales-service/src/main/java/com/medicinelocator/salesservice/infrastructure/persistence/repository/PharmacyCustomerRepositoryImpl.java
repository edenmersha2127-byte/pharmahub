package com.medicinelocator.salesservice.infrastructure.persistence.repository;

import com.medicinelocator.salesservice.application.service.PharmacyCustomerRepository;
import com.medicinelocator.salesservice.domain.model.PharmacyCustomer;
import com.medicinelocator.salesservice.infrastructure.persistence.entity.PharmacyCustomerEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PharmacyCustomerRepositoryImpl implements PharmacyCustomerRepository {

    private final PharmacyCustomerJpaRepository jpaRepository;

    public PharmacyCustomerRepositoryImpl(PharmacyCustomerJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public PharmacyCustomer save(PharmacyCustomer customer) {
        return toDomain(jpaRepository.save(toEntity(customer)));
    }

    @Override
    public PharmacyCustomer update(PharmacyCustomer customer) {
        return toDomain(jpaRepository.save(toEntity(customer)));
    }

    @Override
    public Optional<PharmacyCustomer> findByIdAndPharmacyId(UUID customerId, UUID pharmacyId) {
        return jpaRepository.findByIdAndPharmacyId(customerId, pharmacyId).map(this::toDomain);
    }

    @Override
    public List<PharmacyCustomer> findByPharmacyId(UUID pharmacyId) {
        return jpaRepository.findByPharmacyIdOrderByCreatedAtDesc(pharmacyId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID customerId) {
        jpaRepository.deleteById(customerId);
    }

    private PharmacyCustomerEntity toEntity(PharmacyCustomer customer) {
        PharmacyCustomerEntity entity = new PharmacyCustomerEntity();
        entity.setId(customer.getId());
        entity.setPharmacyId(customer.getPharmacyId());
        entity.setName(customer.getName());
        entity.setPhone(customer.getPhone());
        entity.setEmail(customer.getEmail());
        entity.setNote(customer.getNote());
        entity.setCreatedAt(customer.getCreatedAt());
        return entity;
    }

    private PharmacyCustomer toDomain(PharmacyCustomerEntity entity) {
        return new PharmacyCustomer(
                entity.getId(),
                entity.getPharmacyId(),
                entity.getName(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getNote(),
                entity.getCreatedAt()
        );
    }
}
