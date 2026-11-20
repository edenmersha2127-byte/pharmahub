package com.medicinelocator.reportservice.application.dto.response;

import java.util.List;

/**
 * Paged list of reports (mirrors the shape used by the notification service).
 */
public class ReportListResponse {

    private List<ReportResponse> reports;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public ReportListResponse() {
    }

    public ReportListResponse(List<ReportResponse> reports, int page, int size,
                              long totalElements, int totalPages) {
        this.reports = reports;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<ReportResponse> getReports() { return reports; }
    public void setReports(List<ReportResponse> reports) { this.reports = reports; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
