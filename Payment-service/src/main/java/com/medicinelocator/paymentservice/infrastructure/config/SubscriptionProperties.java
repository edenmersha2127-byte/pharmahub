package com.medicinelocator.paymentservice.infrastructure.config;

import com.medicinelocator.paymentservice.domain.enums.BillingCycle;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Binds the `subscription.*` config: trial length, plan prices/durations, and
 * the pre-expiry reminder window. Prices in ETB.
 */
@Component
@ConfigurationProperties(prefix = "subscription")
public class SubscriptionProperties {

    private int trialDays = 15;
    private int reminderDaysBefore = 3;
    private Plans plans = new Plans();

    public Plan planFor(BillingCycle cycle) {
        return cycle == BillingCycle.YEARLY ? plans.getYearly() : plans.getMonthly();
    }

    public int getTrialDays() { return trialDays; }
    public void setTrialDays(int trialDays) { this.trialDays = trialDays; }

    public int getReminderDaysBefore() { return reminderDaysBefore; }
    public void setReminderDaysBefore(int reminderDaysBefore) { this.reminderDaysBefore = reminderDaysBefore; }

    public Plans getPlans() { return plans; }
    public void setPlans(Plans plans) { this.plans = plans; }

    public static class Plans {
        private Plan monthly = new Plan();
        private Plan yearly = new Plan();

        public Plan getMonthly() { return monthly; }
        public void setMonthly(Plan monthly) { this.monthly = monthly; }

        public Plan getYearly() { return yearly; }
        public void setYearly(Plan yearly) { this.yearly = yearly; }
    }

    public static class Plan {
        private BigDecimal price = BigDecimal.ZERO;
        private int durationDays;

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public int getDurationDays() { return durationDays; }
        public void setDurationDays(int durationDays) { this.durationDays = durationDays; }
    }
}
