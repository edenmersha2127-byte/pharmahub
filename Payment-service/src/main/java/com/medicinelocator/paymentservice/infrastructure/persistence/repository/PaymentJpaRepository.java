package com.medicinelocator.paymentservice.infrastructure.persistence.repository;

import com.medicinelocator.paymentservice.infrastructure.persistence.entity.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {

    Optional<PaymentEntity> findByTxRef(String txRef);

    Page<PaymentEntity> findByPharmacyId(UUID pharmacyId, Pageable pageable);
}
