package com.medicinelocator.inventory.infrastructure.persistence.repository;

import com.medicinelocator.inventory.application.service.InventorySnapshotRepository;
import com.medicinelocator.inventory.domain.model.InventorySnapshot;
import com.medicinelocator.inventory.infrastructure.persistence.entity.InventorySnapshotEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Component
public class InventorySnapshotRepositoryImpl implements InventorySnapshotRepository {

    private final InventorySnapshotJpaRepository jpaRepository;

    public InventorySnapshotRepositoryImpl(InventorySnapshotJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public InventorySnapshot save(InventorySnapshot snapshot) {
        InventorySnapshotEntity entity = new InventorySnapshotEntity();
        entity.setId(snapshot.getId());
        entity.setPharmacyId(snapshot.getPharmacyId());
        entity.setSnapshotDate(snapshot.getSnapshotDate());
        entity.setMedicineCount(snapshot.getMedicineCount());
        entity.setInventoryValue(snapshot.getInventoryValue());
        if (snapshot.getCreatedAt() != null) {
            entity.setCreatedAt(snapshot.getCreatedAt());
        }
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public boolean existsForDate(UUID pharmacyId, LocalDate snapshotDate) {
        return jpaRepository.existsByPharmacyIdAndSnapshotDate(pharmacyId, snapshotDate);
    }

    @Override
    public Optional<InventorySnapshot> findLatestOnOrBefore(UUID pharmacyId, LocalDate date) {
        return jpaRepository
                .findTopByPharmacyIdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(pharmacyId, date)
                .map(this::toDomain);
    }

    private InventorySnapshot toDomain(InventorySnapshotEntity entity) {
        return new InventorySnapshot(
                entity.getId(),
                entity.getPharmacyId(),
                entity.getSnapshotDate(),
                entity.getMedicineCount(),
                entity.getInventoryValue(),
                entity.getCreatedAt()
        );
    }
}
