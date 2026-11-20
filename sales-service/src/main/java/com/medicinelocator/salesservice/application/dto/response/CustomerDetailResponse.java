package com.medicinelocator.salesservice.application.dto.response;

import java.util.List;

/**
 * A customer plus their purchase history.
 */
public record CustomerDetailResponse(
        CustomerResponse customer,
        List<SaleSummaryResponse> purchaseHistory
) {
}
