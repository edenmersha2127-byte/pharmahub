package com.medicinelocator.reportservice.application.service;

import com.medicinelocator.reportservice.application.dto.response.PharmacySummaryResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application-layer port for the pharmacy account actions that the Report
 * Service does not own. Pharmacy state (suspension, strikes) lives in the Auth
 * Service; the only implementation is a REST client to it.
 *
 * Read (getPharmacy) is best-effort and returns empty on failure. The mutating
 * actions (suspend/strike/lift) MUST surface failures so an admin is not told
 * an action succeeded when it did not.
 */
public interface PharmacyModerationService {

    /** Fetch a pharmacy's summary (name/email) for enrichment and notifications. */
    Optional<PharmacySummaryResponse> getPharmacy(UUID pharmacyId);

    /**
     * Report-admin account ids, used to fan out the "pharmacy flagged" alert to
     * the moderators responsible for reports. Best-effort: returns an empty
     * list if Auth is unreachable or no report admins exist yet.
     */
    List<UUID> getReportAdminIds();

    /** Temporarily suspend a pharmacy for the given number of days. */
    void suspend(UUID pharmacyId, int days);

    /** Record a strike (formal warning) against a pharmacy. */
    void addStrike(UUID pharmacyId);

    /** Lift an active suspension immediately. */
    void liftSuspension(UUID pharmacyId);
}
