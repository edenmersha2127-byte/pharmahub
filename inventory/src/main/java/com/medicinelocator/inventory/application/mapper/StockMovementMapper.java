package com.medicinelocator.inventory.application.mapper;

import com.medicinelocator.inventory.application.dto.response.StockMovementResponse;
import com.medicinelocator.inventory.domain.model.StockMovement;
import org.springframework.stereotype.Component;

@Component
public class StockMovementMapper {

    public StockMovementResponse toResponse(StockMovement movement) {
        StockMovementResponse response = new StockMovementResponse();
        response.setId(movement.getId());
        response.setMedicineId(movement.getMedicineId());
        response.setPharmacyId(movement.getPharmacyId());
        response.setType(movement.getType() != null ? movement.getType().name() : null);
        response.setQuantityChange(movement.getQuantityChange());
        response.setResultingQuantity(movement.getResultingQuantity());
        response.setNote(movement.getNote());
        response.setCreatedBy(movement.getCreatedBy());
        response.setCreatedAt(movement.getCreatedAt());
        return response;
    }
}
