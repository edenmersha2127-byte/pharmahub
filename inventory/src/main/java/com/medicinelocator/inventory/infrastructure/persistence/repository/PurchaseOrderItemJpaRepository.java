package com.medicinelocator.inventory.infrastructure.persistence.repository;

import com.medicinelocator.inventory.infrastructure.persistence.entity.PurchaseOrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PurchaseOrderItemJpaRepository extends JpaRepository<PurchaseOrderItemEntity, UUID> {

    List<PurchaseOrderItemEntity> findByPurchaseOrderId(UUID purchaseOrderId);
}
