package com.medicinelocator.paymentservice.application.service;

import com.medicinelocator.paymentservice.domain.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findByTxRef(String txRef);

    Page<Payment> findByPharmacy(UUID pharmacyId, Pageable pageable);

    Page<Payment> findAll(Pageable pageable);
}
