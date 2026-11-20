package com.medicinelocator.paymentservice.application.service;

import com.medicinelocator.paymentservice.domain.model.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository {

    Subscription save(Subscription subscription);

    Optional<Subscription> findById(UUID id);

    /** The pharmacy's current access-granting subscription (TRIAL/ACTIVE, latest end). */
    Optional<Subscription> findCurrentForPharmacy(UUID pharmacyId);

    /** All subscriptions for a pharmacy (history). */
    List<Subscription> findByPharmacy(UUID pharmacyId);

    Page<Subscription> findAll(Pageable pageable);

    /** Active/trial subscriptions whose period has elapsed (for the expiry job). */
    List<Subscription> findExpired(LocalDateTime now);

    /** Active/trial subscriptions expiring within [from, to] that haven't been reminded. */
    List<Subscription> findExpiringSoon(LocalDateTime from, LocalDateTime to);
}
