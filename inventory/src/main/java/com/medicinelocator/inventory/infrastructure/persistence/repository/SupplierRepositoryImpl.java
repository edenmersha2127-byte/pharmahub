package com.medicinelocator.inventory.infrastructure.persistence.repository;

import com.medicinelocator.inventory.application.service.SupplierRepository;
import com.medicinelocator.inventory.domain.model.Supplier;
import com.medicinelocator.inventory.infrastructure.persistence.entity.SupplierEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class SupplierRepositoryImpl implements SupplierRepository {

    private final SupplierJpaRepository jpaRepository;

    public SupplierRepositoryImpl(SupplierJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Supplier save(Supplier supplier) {
        SupplierEntity entity = new SupplierEntity();
        entity.setId(supplier.getId());
        mapDomainToEntity(supplier, entity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Supplier update(Supplier supplier) {
        SupplierEntity entity = jpaRepository.findById(supplier.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Supplier not found for update: " + supplier.getId()));
        mapDomainToEntity(supplier, entity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Supplier> findByIdAndPharmacyId(UUID supplierId, UUID pharmacyId) {
        return jpaRepository.findByIdAndPharmacyId(supplierId, pharmacyId).map(this::toDomain);
    }

    @Override
    public List<Supplier> findByPharmacyId(UUID pharmacyId) {
        return jpaRepository.findByPharmacyIdOrderByNameAsc(pharmacyId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID supplierId) {
        jpaRepository.deleteById(supplierId);
    }

    private void mapDomainToEntity(Supplier supplier, SupplierEntity entity) {
        entity.setPharmacyId(supplier.getPharmacyId());
        entity.setName(supplier.getName());
        entity.setPhone(supplier.getPhone());
        entity.setEmail(supplier.getEmail());
        entity.setAddress(supplier.getAddress());
        entity.setNote(supplier.getNote());
        if (supplier.getCreatedAt() != null) {
            entity.setCreatedAt(supplier.getCreatedAt());
        }
    }

    private Supplier toDomain(SupplierEntity entity) {
        return new Supplier(
                entity.getId(),
                entity.getPharmacyId(),
                entity.getName(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getAddress(),
                entity.getNote(),
                entity.getCreatedAt()
        );
    }
}
