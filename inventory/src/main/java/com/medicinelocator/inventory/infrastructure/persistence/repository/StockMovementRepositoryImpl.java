package com.medicinelocator.inventory.infrastructure.persistence.repository;

import com.medicinelocator.inventory.application.service.StockMovementRepository;
import com.medicinelocator.inventory.domain.model.StockMovement;
import com.medicinelocator.inventory.infrastructure.persistence.entity.StockMovementEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class StockMovementRepositoryImpl implements StockMovementRepository {

    private final StockMovementJpaRepository jpaRepository;

    public StockMovementRepositoryImpl(StockMovementJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public StockMovement save(StockMovement movement) {
        StockMovementEntity entity = new StockMovementEntity();
        entity.setMedicineId(movement.getMedicineId());
        entity.setPharmacyId(movement.getPharmacyId());
        entity.setType(movement.getType());
        entity.setQuantityChange(movement.getQuantityChange());
        entity.setResultingQuantity(movement.getResultingQuantity());
        entity.setNote(movement.getNote());
        entity.setCreatedBy(movement.getCreatedBy());
        if (movement.getCreatedAt() != null) {
            entity.setCreatedAt(movement.getCreatedAt());
        }
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<StockMovement> findByMedicineIdAndPharmacyId(UUID medicineId, UUID pharmacyId) {
        return jpaRepository
                .findByMedicineIdAndPharmacyIdOrderByCreatedAtDesc(medicineId, pharmacyId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockMovement> findByPharmacyIdBetween(UUID pharmacyId, LocalDateTime from, LocalDateTime to) {
        return jpaRepository
                .findByPharmacyIdAndCreatedAtBetweenOrderByCreatedAtAsc(pharmacyId, from, to).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private StockMovement toDomain(StockMovementEntity entity) {
        return new StockMovement(
                entity.getId(),
                entity.getMedicineId(),
                entity.getPharmacyId(),
                entity.getType(),
                entity.getQuantityChange(),
                entity.getResultingQuantity(),
                entity.getNote(),
                entity.getCreatedBy(),
                entity.getCreatedAt()
        );
    }
}
