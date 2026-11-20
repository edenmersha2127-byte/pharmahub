package com.medicinelocator.paymentservice.application.query.handler;

import com.medicinelocator.paymentservice.application.dto.response.PlanResponse;
import com.medicinelocator.paymentservice.domain.enums.BillingCycle;
import com.medicinelocator.paymentservice.infrastructure.config.SubscriptionProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PlanQueryHandler {

    private final SubscriptionProperties properties;
    private final String currency;

    public PlanQueryHandler(SubscriptionProperties properties,
                            @Value("${chapa.currency:ETB}") String currency) {
        this.properties = properties;
        this.currency = currency;
    }

    public List<PlanResponse> list() {
        return List.of(
                planResponse(BillingCycle.MONTHLY),
                planResponse(BillingCycle.YEARLY));
    }

    private PlanResponse planResponse(BillingCycle cycle) {
        SubscriptionProperties.Plan plan = properties.planFor(cycle);
        return new PlanResponse(cycle, plan.getPrice(), plan.getDurationDays(), currency);
    }
}
