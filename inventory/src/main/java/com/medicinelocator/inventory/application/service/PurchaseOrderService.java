package com.medicinelocator.inventory.application.service;

import com.medicinelocator.inventory.application.dto.request.CreatePurchaseOrderRequest;
import com.medicinelocator.inventory.application.dto.request.PurchaseOrderItemRequest;
import com.medicinelocator.inventory.application.dto.response.PharmacyProfileResponse;
import com.medicinelocator.inventory.domain.enums.PurchaseOrderStatus;
import com.medicinelocator.inventory.domain.enums.StockMovementType;
import com.medicinelocator.inventory.domain.exception.MedicineNotFoundException;
import com.medicinelocator.inventory.domain.exception.PurchaseOrderNotFoundException;
import com.medicinelocator.inventory.domain.model.Medicine;
import com.medicinelocator.inventory.domain.model.MedicineBatch;
import com.medicinelocator.inventory.domain.model.PurchaseOrder;
import com.medicinelocator.inventory.domain.model.PurchaseOrderItem;
import com.medicinelocator.inventory.infrastructure.redis.InventoryCacheService;
import com.medicinelocator.inventory.infrastructure.security.CurrentUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Purchase-order lifecycle for a pharmacy: raise a DRAFT order against a
 * supplier, then RECEIVE it — which books each line into stock as a
 * {@link MedicineBatch}, bumps the medicine's denormalized quantity, records a
 * PURCHASE stock movement, and notifies the pharmacy. Receiving is idempotent-
 * guarded: an already-received order cannot be received again.
 */
@Service
public class PurchaseOrderService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseOrderService.class);
    private static final String RECIPIENT_TYPE = "PHARMACY";
    private static final String INVENTORY_LINK = "/pharmacy/inventory";

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final MedicineBatchRepository batchRepository;
    private final MedicineService medicineService;
    private final StockMovementRecorder stockMovementRecorder;
    private final StockNotificationPublisher stockNotificationPublisher;
    private final NotificationClient notificationClient;
    private final InventoryCacheService cacheService;
    private final SearchIndexClient searchIndexClient;
    private final PharmacyProfileService pharmacyProfileService;
    private final CurrentUserProvider currentUserProvider;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                MedicineBatchRepository batchRepository,
                                MedicineService medicineService,
                                StockMovementRecorder stockMovementRecorder,
                                StockNotificationPublisher stockNotificationPublisher,
                                NotificationClient notificationClient,
                                InventoryCacheService cacheService,
                                SearchIndexClient searchIndexClient,
                                PharmacyProfileService pharmacyProfileService,
                                CurrentUserProvider currentUserProvider) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.batchRepository = batchRepository;
        this.medicineService = medicineService;
        this.stockMovementRecorder = stockMovementRecorder;
        this.stockNotificationPublisher = stockNotificationPublisher;
        this.notificationClient = notificationClient;
        this.cacheService = cacheService;
        this.searchIndexClient = searchIndexClient;
        this.pharmacyProfileService = pharmacyProfileService;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public PurchaseOrder create(UUID pharmacyId, CreatePurchaseOrderRequest request) {
        UUID purchaseOrderId = UUID.randomUUID();

        List<PurchaseOrderItem> items = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;
        for (PurchaseOrderItemRequest itemRequest : request.getItems()) {
            PurchaseOrderItem item = new PurchaseOrderItem(
                    UUID.randomUUID(),
                    purchaseOrderId,
                    itemRequest.getMedicineId(),
                    itemRequest.getQuantity(),
                    itemRequest.getUnitCost(),
                    trim(itemRequest.getBatchNumber()),
                    itemRequest.getExpiryDate()
            );
            items.add(item);
            totalCost = totalCost.add(item.lineCost());
        }

        PurchaseOrder purchaseOrder = new PurchaseOrder(
                purchaseOrderId,
                pharmacyId,
                request.getSupplierId(),
                PurchaseOrderStatus.DRAFT,
                totalCost,
                LocalDateTime.now(),
                null
        );
        purchaseOrder.setItems(items);

        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        log.info("Purchase order created: id={} pharmacyId={} items={} totalCost={}",
                saved.getId(), pharmacyId, items.size(), totalCost);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<PurchaseOrder> list(UUID pharmacyId) {
        return purchaseOrderRepository.findByPharmacyId(pharmacyId);
    }

    @Transactional(readOnly = true)
    public PurchaseOrder getById(UUID pharmacyId, UUID purchaseOrderId) {
        return purchaseOrderRepository.findByIdAndPharmacyId(purchaseOrderId, pharmacyId)
                .orElseThrow(() -> new PurchaseOrderNotFoundException(pharmacyId, purchaseOrderId));
    }

    /**
     * Receive a DRAFT purchase order: book every line into stock as a batch and
     * bump the corresponding medicine's quantity. Atomic — any missing medicine
     * rolls back the whole receipt.
     */
    @Transactional
    public PurchaseOrder receive(UUID pharmacyId, UUID purchaseOrderId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findByIdAndPharmacyId(purchaseOrderId, pharmacyId)
                .orElseThrow(() -> new PurchaseOrderNotFoundException(pharmacyId, purchaseOrderId));

        if (purchaseOrder.isReceived()) {
            throw new IllegalArgumentException(
                    "Purchase order " + purchaseOrderId + " has already been received");
        }

        UUID currentUserId = resolveCurrentUserId();
        LocalDateTime now = LocalDateTime.now();
        int totalUnits = 0;

        for (PurchaseOrderItem item : purchaseOrder.getItems()) {
            Medicine medicine = medicineService
                    .findByIdAndPharmacyId(item.getMedicineId(), pharmacyId)
                    .orElseThrow(() -> new MedicineNotFoundException(pharmacyId, item.getMedicineId()));

            int oldQty = medicine.getStockQuantity();

            // Book the received quantity as a new batch (lot) for FEFO tracking.
            MedicineBatch batch = new MedicineBatch(
                    UUID.randomUUID(),
                    medicine.getId(),
                    pharmacyId,
                    item.getBatchNumber(),
                    item.getExpiryDate(),
                    item.getQuantity(),
                    item.getUnitCost(),
                    now
            );
            batchRepository.save(batch);

            // Keep the medicine's denormalized total in sync (= sum of batches).
            medicine.updateStock(oldQty + item.getQuantity());
            if (item.getUnitCost() != null) {
                medicine.setPurchasePrice(item.getUnitCost()); // reflect latest cost
            }
            if (medicine.getSupplierId() == null && purchaseOrder.getSupplierId() != null) {
                medicine.setSupplierId(purchaseOrder.getSupplierId());
            }
            Medicine updated = medicineService.update(medicine);
            totalUnits += item.getQuantity();

            stockMovementRecorder.record(updated, StockMovementType.PURCHASE,
                    item.getQuantity(), updated.getStockQuantity(),
                    "Received via purchase order " + purchaseOrderId, currentUserId);

            pushToSearchIndex(updated);
            stockNotificationPublisher.onStockChanged(updated, oldQty, updated.getStockQuantity());
        }

        purchaseOrder.markReceived(now);
        PurchaseOrder received = purchaseOrderRepository.update(purchaseOrder);
        cacheService.evictPharmacyInventoryCache(pharmacyId);

        notificationClient.notify(pharmacyId, RECIPIENT_TYPE, "PURCHASE_RECEIVED",
                "Stock received",
                "Purchase order received: " + purchaseOrder.getItems().size()
                        + " item(s), " + totalUnits + " units added to inventory.",
                INVENTORY_LINK);

        log.info("Purchase order received: id={} pharmacyId={} units={}",
                purchaseOrderId, pharmacyId, totalUnits);
        return received;
    }

    private String trim(String value) {
        return value != null ? value.trim() : null;
    }

    private UUID resolveCurrentUserId() {
        try {
            return currentUserProvider.getCurrentUser().getUserId();
        } catch (Exception e) {
            return null;
        }
    }

    private void pushToSearchIndex(Medicine medicine) {
        try {
            PharmacyProfileResponse profile = pharmacyProfileService
                    .findById(medicine.getPharmacyId())
                    .orElseThrow(() -> new RuntimeException(
                            "Pharmacy not found in Auth Service for id: " + medicine.getPharmacyId()));

            searchIndexClient.indexMedicine(
                    medicine,
                    profile.getPharmacyName(),
                    profile.getAddress(),
                    profile.getCity(),
                    profile.getLatitude(),
                    profile.getLongitude(),
                    profile.isOpen()
            );
        } catch (Exception e) {
            log.error("Failed to push medicine to search index after receipt: medicineId={} error={}",
                    medicine.getId(), e.getMessage(), e);
        }
    }
}
