package com.medicinelocator.inventory.application.mapper;

import com.medicinelocator.inventory.application.dto.response.PurchaseOrderItemResponse;
import com.medicinelocator.inventory.application.dto.response.PurchaseOrderResponse;
import com.medicinelocator.inventory.domain.model.PurchaseOrder;
import com.medicinelocator.inventory.domain.model.PurchaseOrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PurchaseOrderMapper {

    public PurchaseOrderResponse toResponse(PurchaseOrder purchaseOrder) {
        PurchaseOrderResponse response = new PurchaseOrderResponse();
        response.setId(purchaseOrder.getId());
        response.setPharmacyId(purchaseOrder.getPharmacyId());
        response.setSupplierId(purchaseOrder.getSupplierId());
        response.setStatus(purchaseOrder.getStatus() != null ? purchaseOrder.getStatus().name() : null);
        response.setTotalCost(purchaseOrder.getTotalCost());
        response.setCreatedAt(purchaseOrder.getCreatedAt());
        response.setReceivedAt(purchaseOrder.getReceivedAt());
        List<PurchaseOrderItemResponse> items = purchaseOrder.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());
        response.setItems(items);
        return response;
    }

    public PurchaseOrderItemResponse toItemResponse(PurchaseOrderItem item) {
        PurchaseOrderItemResponse response = new PurchaseOrderItemResponse();
        response.setId(item.getId());
        response.setPurchaseOrderId(item.getPurchaseOrderId());
        response.setMedicineId(item.getMedicineId());
        response.setQuantity(item.getQuantity());
        response.setUnitCost(item.getUnitCost());
        response.setBatchNumber(item.getBatchNumber());
        response.setExpiryDate(item.getExpiryDate());
        return response;
    }
}
