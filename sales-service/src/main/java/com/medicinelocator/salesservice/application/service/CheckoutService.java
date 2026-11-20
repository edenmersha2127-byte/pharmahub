package com.medicinelocator.salesservice.application.service;

import com.medicinelocator.salesservice.application.dto.request.CheckoutItemRequest;
import com.medicinelocator.salesservice.application.dto.request.CheckoutRequest;
import com.medicinelocator.salesservice.domain.exception.PharmacyCustomerNotFoundException;
import com.medicinelocator.salesservice.domain.model.Sale;
import com.medicinelocator.salesservice.domain.model.SaleItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * POS checkout: atomically deducts the cart from inventory, then records the
 * sale with a price snapshot and notifies the pharmacy.
 *
 * <p>Consistency note: stock is deducted in the inventory-service (its own
 * transaction) BEFORE the sale is persisted here, because the authoritative
 * selling price is only known from the deduction response. In the rare event
 * the local save fails after a successful deduction, the error is logged with
 * the deducted lines so it can be reconciled — a full saga/compensation is a
 * future enhancement.</p>
 */
@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);
    private static final String RECIPIENT_TYPE = "PHARMACY";
    private static final String SALES_LINK = "/pharmacy/sales";

    private final InventoryClient inventoryClient;
    private final SaleRepository saleRepository;
    private final PharmacyCustomerRepository customerRepository;
    private final NotificationClient notificationClient;

    public CheckoutService(InventoryClient inventoryClient,
                           SaleRepository saleRepository,
                           PharmacyCustomerRepository customerRepository,
                           NotificationClient notificationClient) {
        this.inventoryClient = inventoryClient;
        this.saleRepository = saleRepository;
        this.customerRepository = customerRepository;
        this.notificationClient = notificationClient;
    }

    public Sale checkout(UUID pharmacyId, UUID cashierId, CheckoutRequest request) {
        // Validate the optional walk-in customer belongs to this pharmacy.
        if (request.getCustomerId() != null
                && customerRepository.findByIdAndPharmacyId(request.getCustomerId(), pharmacyId).isEmpty()) {
            throw new PharmacyCustomerNotFoundException(pharmacyId, request.getCustomerId());
        }

        // Deduct the whole cart atomically in inventory (all-or-nothing).
        List<InventoryClient.Line> lines = request.getItems().stream()
                .map(i -> new InventoryClient.Line(i.getMedicineId(), i.getQuantity()))
                .toList();
        List<InventoryClient.DeductedItem> deducted = inventoryClient.deductForSale(pharmacyId, lines);

        Map<UUID, InventoryClient.DeductedItem> priceByMedicine = new HashMap<>();
        for (InventoryClient.DeductedItem d : deducted) {
            priceByMedicine.put(d.medicineId(), d);
        }

        UUID saleId = UUID.randomUUID();
        List<SaleItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CheckoutItemRequest reqItem : request.getItems()) {
            InventoryClient.DeductedItem snapshot = priceByMedicine.get(reqItem.getMedicineId());
            BigDecimal unitPrice = snapshot != null && snapshot.unitPrice() != null
                    ? snapshot.unitPrice() : BigDecimal.ZERO;
            BigDecimal purchasePrice = snapshot != null ? snapshot.purchasePrice() : null;
            String name = snapshot != null ? snapshot.medicineName() : null;

            SaleItem item = new SaleItem(
                    UUID.randomUUID(), saleId, reqItem.getMedicineId(), name,
                    reqItem.getQuantity(), unitPrice, purchasePrice);
            items.add(item);
            total = total.add(item.lineTotal());
        }

        Sale sale = new Sale(saleId, pharmacyId, request.getCustomerId(), cashierId,
                request.getPaymentMethod(), total, LocalDateTime.now());
        sale.setItems(items);

        Sale saved;
        try {
            saved = saleRepository.save(sale);
        } catch (RuntimeException e) {
            log.error("CRITICAL: stock deducted but sale record failed. pharmacyId={} lines={} error={}",
                    pharmacyId, priceByMedicine.keySet(), e.getMessage(), e);
            throw e;
        }

        notificationClient.notify(pharmacyId, RECIPIENT_TYPE, "SALE_COMPLETED",
                "Sale completed",
                "Sale of " + total + " recorded (" + items.size() + " item(s)).",
                SALES_LINK);

        log.info("Sale recorded: id={} pharmacyId={} cashierId={} total={} items={}",
                saleId, pharmacyId, cashierId, total, items.size());
        return saved;
    }
}
