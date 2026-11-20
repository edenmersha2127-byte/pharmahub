package com.medicinelocator.inventory.infrastructure.persistence.repository;

import com.medicinelocator.inventory.application.service.PurchaseOrderRepository;
import com.medicinelocator.inventory.domain.model.PurchaseOrder;
import com.medicinelocator.inventory.domain.model.PurchaseOrderItem;
import com.medicinelocator.inventory.infrastructure.persistence.entity.PurchaseOrderEntity;
import com.medicinelocator.inventory.infrastructure.persistence.entity.PurchaseOrderItemEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PurchaseOrderRepositoryImpl implements PurchaseOrderRepository {

    private final PurchaseOrderJpaRepository poJpaRepository;
    private final PurchaseOrderItemJpaRepository itemJpaRepository;

    public PurchaseOrderRepositoryImpl(PurchaseOrderJpaRepository poJpaRepository,
                                       PurchaseOrderItemJpaRepository itemJpaRepository) {
        this.poJpaRepository = poJpaRepository;
        this.itemJpaRepository = itemJpaRepository;
    }

    @Override
    public PurchaseOrder save(PurchaseOrder purchaseOrder) {
        PurchaseOrderEntity entity = new PurchaseOrderEntity();
        entity.setId(purchaseOrder.getId());
        mapHeaderToEntity(purchaseOrder, entity);
        PurchaseOrderEntity savedHeader = poJpaRepository.save(entity);

        for (PurchaseOrderItem item : purchaseOrder.getItems()) {
            PurchaseOrderItemEntity itemEntity = new PurchaseOrderItemEntity();
            itemEntity.setId(item.getId());
            itemEntity.setPurchaseOrderId(savedHeader.getId());
            itemEntity.setMedicineId(item.getMedicineId());
            itemEntity.setQuantity(item.getQuantity());
            itemEntity.setUnitCost(item.getUnitCost());
            itemEntity.setBatchNumber(item.getBatchNumber());
            itemEntity.setExpiryDate(item.getExpiryDate());
            item.setPurchaseOrderId(savedHeader.getId());
            PurchaseOrderItemEntity savedItem = itemJpaRepository.save(itemEntity);
            item.setId(savedItem.getId());
        }

        PurchaseOrder result = toDomain(savedHeader);
        result.setItems(purchaseOrder.getItems());
        return result;
    }

    @Override
    public PurchaseOrder update(PurchaseOrder purchaseOrder) {
        PurchaseOrderEntity entity = poJpaRepository.findById(purchaseOrder.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Purchase order not found for update: " + purchaseOrder.getId()));
        mapHeaderToEntity(purchaseOrder, entity);
        PurchaseOrderEntity saved = poJpaRepository.save(entity);
        PurchaseOrder result = toDomain(saved);
        result.setItems(loadItems(saved.getId()));
        return result;
    }

    @Override
    public Optional<PurchaseOrder> findByIdAndPharmacyId(UUID purchaseOrderId, UUID pharmacyId) {
        return poJpaRepository.findByIdAndPharmacyId(purchaseOrderId, pharmacyId)
                .map(entity -> {
                    PurchaseOrder po = toDomain(entity);
                    po.setItems(loadItems(entity.getId()));
                    return po;
                });
    }

    @Override
    public List<PurchaseOrder> findByPharmacyId(UUID pharmacyId) {
        return poJpaRepository.findByPharmacyIdOrderByCreatedAtDesc(pharmacyId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private List<PurchaseOrderItem> loadItems(UUID purchaseOrderId) {
        return itemJpaRepository.findByPurchaseOrderId(purchaseOrderId).stream()
                .map(this::toItemDomain)
                .collect(Collectors.toList());
    }

    private void mapHeaderToEntity(PurchaseOrder po, PurchaseOrderEntity entity) {
        entity.setPharmacyId(po.getPharmacyId());
        entity.setSupplierId(po.getSupplierId());
        entity.setStatus(po.getStatus());
        entity.setTotalCost(po.getTotalCost());
        if (po.getCreatedAt() != null) {
            entity.setCreatedAt(po.getCreatedAt());
        }
        entity.setReceivedAt(po.getReceivedAt());
    }

    private PurchaseOrder toDomain(PurchaseOrderEntity entity) {
        return new PurchaseOrder(
                entity.getId(),
                entity.getPharmacyId(),
                entity.getSupplierId(),
                entity.getStatus(),
                entity.getTotalCost(),
                entity.getCreatedAt(),
                entity.getReceivedAt()
        );
    }

    private PurchaseOrderItem toItemDomain(PurchaseOrderItemEntity entity) {
        return new PurchaseOrderItem(
                entity.getId(),
                entity.getPurchaseOrderId(),
                entity.getMedicineId(),
                entity.getQuantity(),
                entity.getUnitCost(),
                entity.getBatchNumber(),
                entity.getExpiryDate()
        );
    }
}
