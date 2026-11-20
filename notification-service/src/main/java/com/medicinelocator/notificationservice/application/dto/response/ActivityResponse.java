package com.medicinelocator.notificationservice.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A single entry in the dashboard's "Recent Activities" feed, derived from a
 * stored notification. {@code amount} is optional and currently always null —
 * notifications don't carry a structured monetary value; it is present so the
 * producers can populate it later without a contract change.
 */
public record ActivityResponse(
        String type,
        String title,
        String detail,
        BigDecimal amount,
        LocalDateTime at
) {
}
