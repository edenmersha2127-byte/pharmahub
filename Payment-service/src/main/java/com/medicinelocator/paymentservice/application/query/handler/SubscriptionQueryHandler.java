package com.medicinelocator.paymentservice.application.query.handler;

import com.medicinelocator.paymentservice.application.dto.response.SubscriptionListResponse;
import com.medicinelocator.paymentservice.application.dto.response.SubscriptionResponse;
import com.medicinelocator.paymentservice.application.dto.response.SubscriptionStatusResponse;
import com.medicinelocator.paymentservice.application.service.SubscriptionRepository;
import com.medicinelocator.paymentservice.domain.exception.SubscriptionNotFoundException;
import com.medicinelocator.paymentservice.domain.model.Subscription;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
public class SubscriptionQueryHandler {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionQueryHandler(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /** The pharmacy's current access subscription, else its most recent one. */
    @Transactional(readOnly = true)
    public SubscriptionResponse findForPharmacy(UUID pharmacyId) {
        Subscription current = subscriptionRepository.findCurrentForPharmacy(pharmacyId)
                .orElseGet(() -> {
                    List<Subscription> all = subscriptionRepository.findByPharmacy(pharmacyId);
                    return all.isEmpty() ? null : all.get(0);
                });
        if (current == null) {
            throw new SubscriptionNotFoundException(
                    "No subscription found for pharmacy " + pharmacyId);
        }
        return SubscriptionResponse.from(current);
    }

    /** Compact access status for internal callers. */
    @Transactional(readOnly = true)
    public SubscriptionStatusResponse status(UUID pharmacyId) {
        Subscription current = subscriptionRepository.findCurrentForPharmacy(pharmacyId).orElse(null);
        return SubscriptionStatusResponse.of(pharmacyId, current);
    }

    /** Admin: all subscriptions, newest first. */
    @Transactional(readOnly = true)
    public SubscriptionListResponse findAll(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return SubscriptionListResponse.from(subscriptionRepository.findAll(pageable));
    }
}
