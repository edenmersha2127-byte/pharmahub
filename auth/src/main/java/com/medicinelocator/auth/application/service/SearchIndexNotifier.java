package com.medicinelocator.auth.application.service;

import java.util.UUID;

/**
 * Notifies the Search Service of pharmacy-level changes that must be
 * reflected in the denormalized search index (e.g. open/close status).
 */
public interface SearchIndexNotifier {

    /**
     * Propagate the pharmacy's open/close state to every one of its
     * rows in the search index. Best-effort: failures are logged, not thrown.
     */
    void updatePharmacyOpenStatus(UUID pharmacyId, boolean open);
}
