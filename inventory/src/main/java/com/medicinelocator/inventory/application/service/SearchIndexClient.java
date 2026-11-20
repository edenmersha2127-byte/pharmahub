package com.medicinelocator.inventory.application.service;

import com.medicinelocator.inventory.domain.model.Medicine;


public interface SearchIndexClient {


    void indexMedicine(Medicine medicine, String pharmacyName, String address,
                       String city, double latitude, double longitude,
                       boolean pharmacyOpen);
}