package com.medicinelocator.reportservice.application.query.handler;

import com.medicinelocator.reportservice.application.dto.response.FlaggedPharmacyResponse;
import com.medicinelocator.reportservice.application.dto.response.PharmacySummaryResponse;
import com.medicinelocator.reportservice.application.query.GetFlaggedPharmaciesQuery;
import com.medicinelocator.reportservice.application.service.PharmacyModerationService;
import com.medicinelocator.reportservice.application.service.PharmacyReportTally;
import com.medicinelocator.reportservice.application.service.ReportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Builds the admin dashboard of flagged pharmacies (unresolved-report count
 * over the flag threshold), enriched with each pharmacy's name from Auth.
 */
@Component
public class GetFlaggedPharmaciesHandler {

    private final ReportRepository reportRepository;
    private final PharmacyModerationService pharmacyModerationService;
    private final long flagThreshold;
    private final long flagWindowDays;

    public GetFlaggedPharmaciesHandler(ReportRepository reportRepository,
                                       PharmacyModerationService pharmacyModerationService,
                                       @Value("${report.flag-threshold:3}") long flagThreshold,
                                       @Value("${report.flag-window-days:7}") long flagWindowDays) {
        this.reportRepository = reportRepository;
        this.pharmacyModerationService = pharmacyModerationService;
        this.flagThreshold = flagThreshold;
        this.flagWindowDays = flagWindowDays;
    }

    @Transactional(readOnly = true)
    public List<FlaggedPharmacyResponse> handle(GetFlaggedPharmaciesQuery query) {
        LocalDateTime since = LocalDateTime.now().minusDays(flagWindowDays);
        List<PharmacyReportTally> tallies =
                reportRepository.findFlaggedPharmacies(since, flagThreshold);

        return tallies.stream()
                .map(t -> new FlaggedPharmacyResponse(
                        t.pharmacyId(),
                        pharmacyModerationService.getPharmacy(t.pharmacyId())
                                .map(PharmacySummaryResponse::getPharmacyName)
                                .orElse(null),
                        t.unresolvedCount()))
                .toList();
    }
}
