package com.medicinelocator.paymentservice.application.service;

import com.medicinelocator.paymentservice.domain.model.Coupon;

import java.util.List;
import java.util.Optional;

public interface CouponRepository {

    Coupon save(Coupon coupon);

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    List<Coupon> findAll();
}
