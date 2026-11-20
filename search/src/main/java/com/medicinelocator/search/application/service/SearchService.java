package com.medicinelocator.search.application.service;

import com.medicinelocator.search.domain.projection.NearbyMedicineProjection;
import com.medicinelocator.search.domain.projection.PharmacySearchView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface SearchService {


    Page<NearbyMedicineProjection> searchMedicinesByName(String searchTerm,
                                                         Double latitude,
                                                         Double longitude,
                                                         Double radiusKm,
                                                         Boolean requiresPrescription,
                                                         String category,
                                                         Pageable pageable);


    Page<PharmacySearchView> searchNearbyPharmacies(double latitude,
                                                    double longitude,
                                                    double radiusKm,
                                                    Pageable pageable);

    Page<NearbyMedicineProjection> searchByPrescriptionMedicines(List<String> medicineNames,
                                                                 Double latitude,
                                                                 Double longitude,
                                                                 Double radiusKm,
                                                                 Pageable pageable);
}