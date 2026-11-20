package com.medicinelocator.inventory.application.service;

import java.util.UUID;

/**
 * Checks whether a pharmacy currently has an active subscription (or trial),
 * as owned by the Payment Service and surfaced by the Auth Service. Used to
 * gate inventory changes behind the subscription paywall.
 */
public interface SubscriptionAccessChecker {

    boolean hasActiveSubscription(UUID pharmacyId);
}
