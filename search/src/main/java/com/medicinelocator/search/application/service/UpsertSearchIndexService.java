package com.medicinelocator.search.application.service;

import com.medicinelocator.search.application.command.IndexMedicineCommand;

import java.util.UUID;


public interface UpsertSearchIndexService {


    void upsert(IndexMedicineCommand command);

    /**
     * Propagate a pharmacy's open/close status to every one of its index rows.
     * @return number of index rows updated
     */
    int setPharmacyOpen(UUID pharmacyId, boolean open);
}