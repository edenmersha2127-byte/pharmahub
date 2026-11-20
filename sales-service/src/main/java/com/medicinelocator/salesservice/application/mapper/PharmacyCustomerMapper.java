package com.medicinelocator.salesservice.application.mapper;

import com.medicinelocator.salesservice.application.dto.response.CustomerResponse;
import com.medicinelocator.salesservice.domain.model.PharmacyCustomer;
import org.springframework.stereotype.Component;

@Component
public class PharmacyCustomerMapper {

    public CustomerResponse toResponse(PharmacyCustomer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getPharmacyId(),
                customer.getName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getNote(),
                customer.getCreatedAt());
    }
}
