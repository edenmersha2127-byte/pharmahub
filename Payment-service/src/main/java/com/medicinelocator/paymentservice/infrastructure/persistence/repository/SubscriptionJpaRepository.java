package com.medicinelocator.paymentservice.infrastructure.persistence.repository;

import com.medicinelocator.paymentservice.domain.enums.SubscriptionStatus;
import com.medicinelocator.paymentservice.infrastructure.persistence.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionEntity, UUID> {

    List<SubscriptionEntity> findByPharmacyIdOrderByCreatedAtDesc(UUID pharmacyId);

    Optional<SubscriptionEntity> findFirstByPharmacyIdAndStatusInOrderByEndDateDesc(
            UUID pharmacyId, Collection<SubscriptionStatus> statuses);

    List<SubscriptionEntity> findByStatusInAndEndDateBefore(
            Collection<SubscriptionStatus> statuses, LocalDateTime cutoff);

    List<SubscriptionEntity> findByStatusInAndReminderSentFalseAndEndDateBetween(
            Collection<SubscriptionStatus> statuses, LocalDateTime from, LocalDateTime to);
}
