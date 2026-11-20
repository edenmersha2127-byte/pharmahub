package com.medicinelocator.reportservice.application.mapper;

import com.medicinelocator.reportservice.application.dto.response.ReportListResponse;
import com.medicinelocator.reportservice.application.dto.response.ReportResponse;
import com.medicinelocator.reportservice.domain.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ReportMapper {

    public ReportResponse toResponse(Report report) {
        ReportResponse response = new ReportResponse();
        response.setId(report.getId());
        response.setReporterId(report.getReporterId());
        response.setPharmacyId(report.getPharmacyId());
        response.setCategory(report.getCategory());
        response.setDescription(report.getDescription());
        response.setStatus(report.getStatus());
        response.setPharmacyResponse(report.getPharmacyResponse());
        response.setResolutionNote(report.getResolutionNote());
        response.setResolvedBy(report.getResolvedBy());
        response.setCreatedAt(report.getCreatedAt());
        response.setUpdatedAt(report.getUpdatedAt());
        return response;
    }

    public ReportListResponse toListResponse(Page<Report> page) {
        return new ReportListResponse(
                page.getContent().stream().map(this::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
