package com.medicinelocator.paymentservice.infrastructure.persistence.repository;

import com.medicinelocator.paymentservice.application.service.SubscriptionRepository;
import com.medicinelocator.paymentservice.domain.enums.SubscriptionStatus;
import com.medicinelocator.paymentservice.domain.model.Subscription;
import com.medicinelocator.paymentservice.infrastructure.persistence.entity.SubscriptionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SubscriptionRepositoryImpl implements SubscriptionRepository {

    private static final List<SubscriptionStatus> ACCESS_STATUSES =
            List.of(SubscriptionStatus.TRIAL, SubscriptionStatus.ACTIVE);

    private final SubscriptionJpaRepository jpaRepository;

    public SubscriptionRepositoryImpl(SubscriptionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Subscription save(Subscription subscription) {
        return toDomain(jpaRepository.save(toEntity(subscription)));
    }

    @Override
    public Optional<Subscription> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Subscription> findCurrentForPharmacy(UUID pharmacyId) {
        return jpaRepository
                .findFirstByPharmacyIdAndStatusInOrderByEndDateDesc(pharmacyId, ACCESS_STATUSES)
                .map(this::toDomain);
    }

    @Override
    public List<Subscription> findByPharmacy(UUID pharmacyId) {
        return jpaRepository.findByPharmacyIdOrderByCreatedAtDesc(pharmacyId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Page<Subscription> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(this::toDomain);
    }

    @Override
    public List<Subscription> findExpired(LocalDateTime now) {
        return jpaRepository.findByStatusInAndEndDateBefore(ACCESS_STATUSES, now)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<Subscription> findExpiringSoon(LocalDateTime from, LocalDateTime to) {
        return jpaRepository
                .findByStatusInAndReminderSentFalseAndEndDateBetween(ACCESS_STATUSES, from, to)
                .stream().map(this::toDomain).toList();
    }

    private SubscriptionEntity toEntity(Subscription s) {
        SubscriptionEntity e = new SubscriptionEntity();
        e.setId(s.getId());
        e.setPharmacyId(s.getPharmacyId());
        e.setPlan(s.getPlan());
        e.setStatus(s.getStatus());
        e.setAmount(s.getAmount());
        e.setStartDate(s.getStartDate());
        e.setEndDate(s.getEndDate());
        e.setReminderSent(s.isReminderSent());
        e.setCreatedAt(s.getCreatedAt());
        e.setUpdatedAt(s.getUpdatedAt());
        return e;
    }

    private Subscription toDomain(SubscriptionEntity e) {
        return new Subscription(
                e.getId(), e.getPharmacyId(), e.getPlan(), e.getStatus(), e.getAmount(),
                e.getStartDate(), e.getEndDate(), e.isReminderSent(),
                e.getCreatedAt(), e.getUpdatedAt());
    }
}
