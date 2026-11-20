package com.medicinelocator.paymentservice.infrastructure.persistence.repository;

import com.medicinelocator.paymentservice.application.service.CouponRepository;
import com.medicinelocator.paymentservice.domain.model.Coupon;
import com.medicinelocator.paymentservice.infrastructure.persistence.entity.CouponEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository jpaRepository;

    public CouponRepositoryImpl(CouponJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Coupon save(Coupon coupon) {
        return toDomain(jpaRepository.save(toEntity(coupon)));
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return jpaRepository.findByCode(code).map(this::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public List<Coupon> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    private CouponEntity toEntity(Coupon c) {
        CouponEntity e = new CouponEntity();
        e.setId(c.getId());
        e.setCode(c.getCode());
        e.setDiscountType(c.getDiscountType());
        e.setDiscountValue(c.getDiscountValue());
        e.setMaxRedemptions(c.getMaxRedemptions());
        e.setTimesRedeemed(c.getTimesRedeemed());
        e.setExpiresAt(c.getExpiresAt());
        e.setActive(c.isActive());
        e.setCreatedAt(c.getCreatedAt());
        e.setUpdatedAt(c.getUpdatedAt());
        return e;
    }

    private Coupon toDomain(CouponEntity e) {
        return new Coupon(
                e.getId(), e.getCode(), e.getDiscountType(), e.getDiscountValue(),
                e.getMaxRedemptions(), e.getTimesRedeemed(), e.getExpiresAt(),
                e.isActive(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
