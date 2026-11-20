package com.medicinelocator.paymentservice.infrastructure.persistence.repository;

import com.medicinelocator.paymentservice.application.service.PaymentRepository;
import com.medicinelocator.paymentservice.domain.model.Payment;
import com.medicinelocator.paymentservice.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    public PaymentRepositoryImpl(PaymentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Payment save(Payment payment) {
        return toDomain(jpaRepository.save(toEntity(payment)));
    }

    @Override
    public Optional<Payment> findByTxRef(String txRef) {
        return jpaRepository.findByTxRef(txRef).map(this::toDomain);
    }

    @Override
    public Page<Payment> findByPharmacy(UUID pharmacyId, Pageable pageable) {
        return jpaRepository.findByPharmacyId(pharmacyId, pageable).map(this::toDomain);
    }

    @Override
    public Page<Payment> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(this::toDomain);
    }

    private PaymentEntity toEntity(Payment p) {
        PaymentEntity e = new PaymentEntity();
        e.setId(p.getId());
        e.setSubscriptionId(p.getSubscriptionId());
        e.setPharmacyId(p.getPharmacyId());
        e.setTxRef(p.getTxRef());
        e.setAmount(p.getAmount());
        e.setCurrency(p.getCurrency());
        e.setStatus(p.getStatus());
        e.setCouponCode(p.getCouponCode());
        e.setCheckoutUrl(p.getCheckoutUrl());
        e.setRawPayload(p.getRawPayload());
        e.setCreatedAt(p.getCreatedAt());
        e.setUpdatedAt(p.getUpdatedAt());
        return e;
    }

    private Payment toDomain(PaymentEntity e) {
        return new Payment(
                e.getId(), e.getSubscriptionId(), e.getPharmacyId(), e.getTxRef(), e.getAmount(),
                e.getCurrency(), e.getStatus(), e.getCouponCode(), e.getCheckoutUrl(),
                e.getRawPayload(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
