package com.medicinelocator.paymentservice.application.query.handler;

import com.medicinelocator.paymentservice.application.dto.response.CouponResponse;
import com.medicinelocator.paymentservice.application.service.CouponRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class CouponQueryHandler {

    private final CouponRepository couponRepository;

    public CouponQueryHandler(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> findAll() {
        return couponRepository.findAll().stream().map(CouponResponse::from).toList();
    }
}
