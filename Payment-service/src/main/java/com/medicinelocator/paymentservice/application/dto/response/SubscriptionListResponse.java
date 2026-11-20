package com.medicinelocator.paymentservice.application.dto.response;

import com.medicinelocator.paymentservice.domain.model.Subscription;
import org.springframework.data.domain.Page;

import java.util.List;

public record SubscriptionListResponse(
        List<SubscriptionResponse> subscriptions,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static SubscriptionListResponse from(Page<Subscription> page) {
        return new SubscriptionListResponse(
                page.getContent().stream().map(SubscriptionResponse::from).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
