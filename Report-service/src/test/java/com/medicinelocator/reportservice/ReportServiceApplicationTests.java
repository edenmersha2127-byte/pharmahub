package com.medicinelocator.reportservice;

import com.medicinelocator.reportservice.domain.enums.ReportCategory;
import com.medicinelocator.reportservice.domain.enums.ReportStatus;
import com.medicinelocator.reportservice.domain.exception.ReportAccessDeniedException;
import com.medicinelocator.reportservice.domain.model.Report;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure-domain unit tests for the Report aggregate. Kept free of Spring context
 * so the build does not require a running database.
 */
class ReportServiceApplicationTests {

    private final UUID reporter = UUID.randomUUID();
    private final UUID pharmacy = UUID.randomUUID();

    @Test
    void newReportIsPendingAndUnresolved() {
        Report report = Report.create(reporter, pharmacy, ReportCategory.PHARMACY_CLOSED, "Was closed");
        assertEquals(ReportStatus.PENDING, report.getStatus());
        assertTrue(report.isUnresolved());
    }

    @Test
    void pharmacyResponseMovesReportUnderReview() {
        Report report = Report.create(reporter, pharmacy, ReportCategory.WRONG_PRICE, "Overcharged");
        report.respondAsPharmacy(pharmacy, "We corrected the price.");
        assertEquals(ReportStatus.UNDER_REVIEW, report.getStatus());
        assertEquals("We corrected the price.", report.getPharmacyResponse());
    }

    @Test
    void foreignPharmacyCannotRespond() {
        Report report = Report.create(reporter, pharmacy, ReportCategory.OTHER, "Issue");
        assertThrows(ReportAccessDeniedException.class,
                () -> report.respondAsPharmacy(UUID.randomUUID(), "not mine"));
    }

    @Test
    void resolveClosesTheReport() {
        Report report = Report.create(reporter, pharmacy, ReportCategory.MEDICINE_UNAVAILABLE, "No stock");
        UUID admin = UUID.randomUUID();
        report.resolve(admin, "Handled");
        assertEquals(ReportStatus.RESOLVED, report.getStatus());
        assertEquals(admin, report.getResolvedBy());
        assertFalse(report.isUnresolved());
    }
}
