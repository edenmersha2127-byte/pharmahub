package com.medicinelocator.auth.application.service;

import java.util.UUID;

/**
 * Asks the Payment Service to start a pharmacy's free trial (on approval).
 * Best-effort: implementations must never throw.
 */
public interface SubscriptionTrialNotifier {

    void startTrial(UUID pharmacyId);
}
