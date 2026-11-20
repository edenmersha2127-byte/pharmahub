package com.medicinelocator.reportservice.infrastructure.persistence.repository;

import java.util.UUID;

/**
 * Spring Data projection for the grouped "flagged pharmacies" query.
 * Field names match the aliases in the JPQL SELECT.
 */
public interface PharmacyReportCountView {

    UUID getPharmacyId();

    long getUnresolvedCount();
}
