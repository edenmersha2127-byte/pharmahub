package com.medicinelocator.paymentservice.application.query.handler;

import com.medicinelocator.paymentservice.application.dto.response.PaymentListResponse;
import com.medicinelocator.paymentservice.application.service.PaymentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class PaymentQueryHandler {

    private final PaymentRepository paymentRepository;

    public PaymentQueryHandler(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public PaymentListResponse findForPharmacy(UUID pharmacyId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PaymentListResponse.from(paymentRepository.findByPharmacy(pharmacyId, pageable));
    }

    @Transactional(readOnly = true)
    public PaymentListResponse findAll(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PaymentListResponse.from(paymentRepository.findAll(pageable));
    }
}
