package com.medicinelocator.inventory.application.mapper;

import com.medicinelocator.inventory.application.dto.response.SupplierResponse;
import com.medicinelocator.inventory.domain.model.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public SupplierResponse toResponse(Supplier supplier) {
        SupplierResponse response = new SupplierResponse();
        response.setId(supplier.getId());
        response.setPharmacyId(supplier.getPharmacyId());
        response.setName(supplier.getName());
        response.setPhone(supplier.getPhone());
        response.setEmail(supplier.getEmail());
        response.setAddress(supplier.getAddress());
        response.setNote(supplier.getNote());
        response.setCreatedAt(supplier.getCreatedAt());
        return response;
    }
}
