package com.medicinelocator.salesservice.application.service;

import com.medicinelocator.salesservice.domain.model.Sale;
import com.medicinelocator.salesservice.domain.model.SaleItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SaleRepository {

    /** Persist a sale together with its items. */
    Sale save(Sale sale);

    /** Load a sale (with items populated) scoped to a pharmacy. */
    Optional<Sale> findByIdAndPharmacyId(UUID saleId, UUID pharmacyId);

    /** Sales for a pharmacy, newest first (headers only, items not populated). */
    List<Sale> findByPharmacyId(UUID pharmacyId);

    /** Sales for one walk-in customer, newest first (headers only). */
    List<Sale> findByCustomer(UUID customerId, UUID pharmacyId);

    /** Sales for a pharmacy within a time window (headers only). */
    List<Sale> findBetween(UUID pharmacyId, LocalDateTime from, LocalDateTime to);

    /** All sale-item lines for a pharmacy within a time window (for analytics). */
    List<SaleItem> findItemsBetween(UUID pharmacyId, LocalDateTime from, LocalDateTime to);
}
