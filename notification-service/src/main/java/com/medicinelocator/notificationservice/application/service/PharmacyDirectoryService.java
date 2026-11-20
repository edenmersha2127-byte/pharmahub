package com.medicinelocator.notificationservice.application.service;

import com.medicinelocator.notificationservice.application.dto.response.PharmacySummaryResponse;

import java.util.List;

/**
 * Application-layer port for resolving the audience of pharmacy-wide
 * notifications (e.g. the nightly "update your medicines" reminder).
 *
 * The pharmacy directory is owned by the Auth Service; the only
 * implementation is a REST client. This service must never read pharmacy
 * data from a local table.
 */
public interface PharmacyDirectoryService {

    /**
     * @return every pharmacy known to the Auth Service, including its status.
     */
    List<PharmacySummaryResponse> findAllPharmacies();
}
