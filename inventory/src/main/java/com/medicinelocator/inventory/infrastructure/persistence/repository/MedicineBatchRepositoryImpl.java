package com.medicinelocator.inventory.infrastructure.persistence.repository;

import com.medicinelocator.inventory.application.service.MedicineBatchRepository;
import com.medicinelocator.inventory.domain.model.MedicineBatch;
import com.medicinelocator.inventory.infrastructure.persistence.entity.MedicineBatchEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class MedicineBatchRepositoryImpl implements MedicineBatchRepository {

    private final MedicineBatchJpaRepository jpaRepository;

    public MedicineBatchRepositoryImpl(MedicineBatchJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MedicineBatch save(MedicineBatch batch) {
        MedicineBatchEntity entity = (batch.getId() != null)
                ? jpaRepository.findById(batch.getId()).orElseGet(MedicineBatchEntity::new)
                : new MedicineBatchEntity();
        if (batch.getId() != null) {
            entity.setId(batch.getId());
        }
        mapDomainToEntity(batch, entity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<MedicineBatch> findByMedicineIdFefo(UUID medicineId) {
        return jpaRepository.findByMedicineIdOrderByExpiryDateAsc(medicineId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicineBatch> findConsumableByMedicineIdFefo(UUID medicineId) {
        return jpaRepository
                .findByMedicineIdAndQuantityGreaterThanOrderByExpiryDateAsc(medicineId, 0).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicineBatch> findExpiringBetween(UUID pharmacyId, LocalDate from, LocalDate to) {
        return jpaRepository.findExpiringBetween(pharmacyId, from, to).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicineBatch> findExpired(UUID pharmacyId, LocalDate today) {
        return jpaRepository.findExpired(pharmacyId, today).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MedicineBatch> findExpiringBetweenAllPharmacies(LocalDate from, LocalDate to) {
        return jpaRepository.findAllExpiringBetween(from, to).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private void mapDomainToEntity(MedicineBatch batch, MedicineBatchEntity entity) {
        entity.setMedicineId(batch.getMedicineId());
        entity.setPharmacyId(batch.getPharmacyId());
        entity.setBatchNumber(batch.getBatchNumber());
        entity.setExpiryDate(batch.getExpiryDate());
        entity.setQuantity(batch.getQuantity());
        entity.setPurchasePrice(batch.getPurchasePrice());
        if (batch.getReceivedAt() != null) {
            entity.setReceivedAt(batch.getReceivedAt());
        }
    }

    private MedicineBatch toDomain(MedicineBatchEntity entity) {
        return new MedicineBatch(
                entity.getId(),
                entity.getMedicineId(),
                entity.getPharmacyId(),
                entity.getBatchNumber(),
                entity.getExpiryDate(),
                entity.getQuantity(),
                entity.getPurchasePrice(),
                entity.getReceivedAt()
        );
    }
}
