package com.medicinelocator.reportservice.application.query.handler;

import com.medicinelocator.reportservice.application.dto.response.ReportListResponse;
import com.medicinelocator.reportservice.application.mapper.ReportMapper;
import com.medicinelocator.reportservice.application.query.GetReportsForPharmacyQuery;
import com.medicinelocator.reportservice.application.service.ReportRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GetReportsForPharmacyHandler {

    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;

    public GetReportsForPharmacyHandler(ReportRepository reportRepository, ReportMapper reportMapper) {
        this.reportRepository = reportRepository;
        this.reportMapper = reportMapper;
    }

    @Transactional(readOnly = true)
    public ReportListResponse handle(GetReportsForPharmacyQuery query) {
        PageRequest pageable = PageRequest.of(
                query.getPage(), query.getSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        return reportMapper.toListResponse(
                reportRepository.findByPharmacy(query.getPharmacyId(), pageable));
    }
}
