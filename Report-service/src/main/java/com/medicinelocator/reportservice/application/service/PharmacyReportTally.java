package com.medicinelocator.reportservice.application.service;

import java.util.UUID;

/**
 * Aggregate count of a pharmacy's unresolved reports, used to build the
 * admin "flagged pharmacies" dashboard.
 */
public record PharmacyReportTally(UUID pharmacyId, long unresolvedCount) {
}
