package com.medicinelocator.paymentservice.application.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PaymentListResponse(
        List<PaymentResponse> payments,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static PaymentListResponse from(Page<com.medicinelocator.paymentservice.domain.model.Payment> page) {
        return new PaymentListResponse(
                page.getContent().stream().map(PaymentResponse::from).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
