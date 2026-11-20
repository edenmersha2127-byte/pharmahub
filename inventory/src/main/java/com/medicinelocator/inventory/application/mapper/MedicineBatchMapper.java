package com.medicinelocator.inventory.application.mapper;

import com.medicinelocator.inventory.application.dto.response.MedicineBatchResponse;
import com.medicinelocator.inventory.domain.model.MedicineBatch;
import org.springframework.stereotype.Component;

@Component
public class MedicineBatchMapper {

    public MedicineBatchResponse toResponse(MedicineBatch batch) {
        MedicineBatchResponse response = new MedicineBatchResponse();
        response.setId(batch.getId());
        response.setMedicineId(batch.getMedicineId());
        response.setPharmacyId(batch.getPharmacyId());
        response.setBatchNumber(batch.getBatchNumber());
        response.setExpiryDate(batch.getExpiryDate());
        response.setQuantity(batch.getQuantity());
        response.setPurchasePrice(batch.getPurchasePrice());
        response.setReceivedAt(batch.getReceivedAt());
        return response;
    }
}
