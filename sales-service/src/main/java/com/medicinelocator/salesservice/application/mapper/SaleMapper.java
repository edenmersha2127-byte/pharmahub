package com.medicinelocator.salesservice.application.mapper;

import com.medicinelocator.salesservice.application.dto.response.ReceiptItemResponse;
import com.medicinelocator.salesservice.application.dto.response.ReceiptResponse;
import com.medicinelocator.salesservice.application.dto.response.SaleSummaryResponse;
import com.medicinelocator.salesservice.domain.model.Sale;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SaleMapper {

    public ReceiptResponse toReceipt(Sale sale) {
        List<ReceiptItemResponse> items = sale.getItems().stream()
                .map(item -> new ReceiptItemResponse(
                        item.getMedicineId(),
                        item.getMedicineName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.lineTotal()))
                .toList();

        return new ReceiptResponse(
                sale.getId(),
                sale.getPharmacyId(),
                sale.getCustomerId(),
                sale.getCashierId(),
                sale.getPaymentMethod() != null ? sale.getPaymentMethod().name() : null,
                sale.getTotal(),
                sale.getCreatedAt(),
                items);
    }

    public SaleSummaryResponse toSummary(Sale sale) {
        return new SaleSummaryResponse(
                sale.getId(),
                sale.getCustomerId(),
                sale.getPaymentMethod() != null ? sale.getPaymentMethod().name() : null,
                sale.getTotal(),
                sale.getCreatedAt());
    }
}
