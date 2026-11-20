package com.medicinelocator.inventory.application.service;

import com.medicinelocator.inventory.domain.model.PurchaseOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence port for purchase orders and their items.
 */
public interface PurchaseOrderRepository {

    /** Persist a new purchase order together with its items. */
    PurchaseOrder save(PurchaseOrder purchaseOrder);

    /** Persist changes to an existing purchase order header (status, totals, receivedAt). */
    PurchaseOrder update(PurchaseOrder purchaseOrder);

    /** Load a purchase order (with items populated) scoped to a pharmacy. */
    Optional<PurchaseOrder> findByIdAndPharmacyId(UUID purchaseOrderId, UUID pharmacyId);

    /** List purchase orders for a pharmacy (headers only, items not populated). */
    List<PurchaseOrder> findByPharmacyId(UUID pharmacyId);
}
