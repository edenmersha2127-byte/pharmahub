package com.medicinelocator.paymentservice;

import com.medicinelocator.paymentservice.domain.enums.CouponDiscountType;
import com.medicinelocator.paymentservice.domain.exception.CouponException;
import com.medicinelocator.paymentservice.domain.model.Coupon;
import com.medicinelocator.paymentservice.domain.model.Subscription;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure-domain unit tests (no Spring context, so no DB needed to build).
 */
class PaymentServiceApplicationTests {

    @Test
    void percentageCouponReducesPrice() {
        Coupon c = Coupon.create("SAVE20", CouponDiscountType.PERCENTAGE, BigDecimal.valueOf(20), null, null);
        assertEquals(0, c.apply(BigDecimal.valueOf(1000)).compareTo(BigDecimal.valueOf(800)));
    }

    @Test
    void fixedCouponNeverGoesNegative() {
        Coupon c = Coupon.create("BIG", CouponDiscountType.FIXED, BigDecimal.valueOf(5000), null, null);
        assertEquals(0, c.apply(BigDecimal.valueOf(1000)).compareTo(BigDecimal.ZERO));
    }

    @Test
    void inactiveCouponRejected() {
        Coupon c = Coupon.create("OFF", CouponDiscountType.FIXED, BigDecimal.TEN, null, null);
        c.setActive(false);
        assertThrows(CouponException.class, c::assertUsable);
    }

    @Test
    void exhaustedCouponRejected() {
        Coupon c = Coupon.create("ONCE", CouponDiscountType.FIXED, BigDecimal.TEN, 1, null);
        c.redeem();
        assertThrows(CouponException.class, c::assertUsable);
    }

    @Test
    void trialGrantsAccessAndExpiredDoesNot() {
        Subscription trial = Subscription.createTrial(UUID.randomUUID(), 15);
        assertTrue(trial.grantsAccess());
        trial.setEndDate(LocalDateTime.now().minusDays(1));
        assertFalse(trial.grantsAccess());
    }
}
