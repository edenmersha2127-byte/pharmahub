package com.medicinelocator.auth.application.mapper;

import com.medicinelocator.auth.application.dto.response.PharmacyAdminResponse;
import com.medicinelocator.auth.application.dto.response.UserAdminResponse;
import com.medicinelocator.auth.domain.enums.Role;
import com.medicinelocator.auth.domain.model.Admin;
import com.medicinelocator.auth.domain.model.Customer;
import com.medicinelocator.auth.domain.model.Pharmacy;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {

    public PharmacyAdminResponse toPharmacyAdminResponse(Pharmacy pharmacy) {
        PharmacyAdminResponse response = new PharmacyAdminResponse();
        response.setId(pharmacy.getId());
        response.setEmail(pharmacy.getEmail());
        response.setPharmacyName(pharmacy.getPharmacyName());
        response.setLicenseNumber(pharmacy.getLicenseNumber());
        response.setPhoneNumber(pharmacy.getPhoneNumber());
        response.setAddress(pharmacy.getAddress());
        response.setCity(pharmacy.getCity());
        response.setLatitude(pharmacy.getLatitude());
        response.setLongitude(pharmacy.getLongitude());
        response.setAccountStatus(pharmacy.getAccountStatus());
        response.setPharmacyStatus(pharmacy.getPharmacyStatus());
        response.setEmailVerified(pharmacy.isEmailVerified());
        response.setCreatedAt(pharmacy.getCreatedAt());
        response.setUpdatedAt(pharmacy.getUpdatedAt());
        return response;
    }

    public UserAdminResponse toUserAdminResponse(Customer customer) {
        return new UserAdminResponse(
                customer.getId(),
                customer.getEmail(),
                customer.getFirstName() + " " + customer.getLastName(),
                Role.CUSTOMER,
                customer.getAccountStatus(),
                customer.isEmailVerified(),
                customer.getCreatedAt()
        );
    }

    public UserAdminResponse toUserAdminResponse(Pharmacy pharmacy) {
        return new UserAdminResponse(
                pharmacy.getId(),
                pharmacy.getEmail(),
                pharmacy.getPharmacyName(),
                Role.PHARMACY,
                pharmacy.getAccountStatus(),
                pharmacy.isEmailVerified(),
                pharmacy.getCreatedAt()
        );
    }

    public UserAdminResponse toUserAdminResponse(Admin admin) {
        return new UserAdminResponse(
                admin.getId(),
                admin.getEmail(),
                admin.getFirstName() + " " + admin.getLastName(),
                Role.ADMIN,
                admin.getAccountStatus(),
                true,
                admin.getCreatedAt()
        );
    }
}