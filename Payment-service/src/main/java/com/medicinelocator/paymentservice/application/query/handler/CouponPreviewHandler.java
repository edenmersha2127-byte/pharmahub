package com.medicinelocator.paymentservice.application.query.handler;

import com.medicinelocator.paymentservice.application.dto.response.CouponPreviewResponse;
import com.medicinelocator.paymentservice.application.service.CouponRepository;
import com.medicinelocator.paymentservice.domain.enums.BillingCycle;
import com.medicinelocator.paymentservice.domain.exception.CouponException;
import com.medicinelocator.paymentservice.domain.model.Coupon;
import com.medicinelocator.paymentservice.infrastructure.config.SubscriptionProperties;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Previews the effect of a coupon on a plan's price without creating anything.
 * Returns a valid=false result (not an error) for bad codes so the UI can show
 * the reason inline.
 */
@Component
public class CouponPreviewHandler {

    private final CouponRepository couponRepository;
    private final SubscriptionProperties properties;

    public CouponPreviewHandler(CouponRepository couponRepository,
                                SubscriptionProperties properties) {
        this.couponRepository = couponRepository;
        this.properties = properties;
    }

    @Transactional(readOnly = true)
    public CouponPreviewResponse preview(BillingCycle plan, String code) {
        BigDecimal price = properties.planFor(plan).getPrice();

        if (code == null || code.isBlank()) {
            return new CouponPreviewResponse(true, "No coupon applied", price, price);
        }

        Optional<Coupon> couponOpt = couponRepository.findByCode(code.trim().toUpperCase());
        if (couponOpt.isEmpty()) {
            return new CouponPreviewResponse(false, "Coupon code not found", price, price);
        }

        Coupon coupon = couponOpt.get();
        try {
            coupon.assertUsable();
        } catch (CouponException e) {
            return new CouponPreviewResponse(false, e.getMessage(), price, price);
        }

        return new CouponPreviewResponse(true, "Coupon applied", price, coupon.apply(price));
    }
}
