package com.medicinelocator.reportservice.application.query.handler;

import com.medicinelocator.reportservice.application.dto.response.ReportListResponse;
import com.medicinelocator.reportservice.application.dto.response.ReportResponse;
import com.medicinelocator.reportservice.application.mapper.ReportMapper;
import com.medicinelocator.reportservice.application.query.GetReportQueueQuery;
import com.medicinelocator.reportservice.application.service.ReportRepository;
import com.medicinelocator.reportservice.domain.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class GetReportQueueHandler {

    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;

    public GetReportQueueHandler(ReportRepository reportRepository, ReportMapper reportMapper) {
        this.reportRepository = reportRepository;
        this.reportMapper = reportMapper;
    }

    @Transactional(readOnly = true)
    public ReportListResponse handle(GetReportQueueQuery query) {
        PageRequest pageable = PageRequest.of(
                query.getPage(), query.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Report> page = reportRepository.findByStatus(query.getStatus(), pageable);

        // Enrich each row with how many times its pharmacy has been reported
        // (lifetime, all statuses) so the admin can spot repeat offenders.
        // Counted once per distinct pharmacy in the page.
        Map<UUID, Long> countCache = new HashMap<>();
        List<ReportResponse> rows = page.getContent().stream().map(report -> {
            ReportResponse response = reportMapper.toResponse(report);
            long count = countCache.computeIfAbsent(
                    report.getPharmacyId(), reportRepository::countByPharmacy);
            response.setPharmacyReportCount(count);
            return response;
        }).toList();

        return new ReportListResponse(
                rows,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
