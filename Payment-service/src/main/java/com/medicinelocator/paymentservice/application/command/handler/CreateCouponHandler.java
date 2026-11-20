package com.medicinelocator.paymentservice.application.command.handler;

import com.medicinelocator.paymentservice.application.command.CreateCouponCommand;
import com.medicinelocator.paymentservice.application.service.CouponRepository;
import com.medicinelocator.paymentservice.domain.model.Coupon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a coupon code (super admin). Codes are unique and stored uppercased.
 */
@Component
public class CreateCouponHandler {

    private static final Logger log = LoggerFactory.getLogger(CreateCouponHandler.class);

    private final CouponRepository couponRepository;

    public CreateCouponHandler(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Transactional
    public Coupon handle(CreateCouponCommand command) {
        String code = command.getCode().trim().toUpperCase();
        if (couponRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Coupon code already exists: " + code);
        }
        Coupon coupon = couponRepository.save(Coupon.create(
                code, command.getDiscountType(), command.getDiscountValue(),
                command.getMaxRedemptions(), command.getExpiresAt()));
        log.info("Coupon created: code={} type={} value={}",
                coupon.getCode(), coupon.getDiscountType(), coupon.getDiscountValue());
        return coupon;
    }
}
