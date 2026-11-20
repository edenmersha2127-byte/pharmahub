package com.medicinelocator.reportservice.application.dto.response;

import java.util.List;

public class AuditLogListResponse {

    private List<AuditLogResponse> logs;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public AuditLogListResponse() {
    }

    public AuditLogListResponse(List<AuditLogResponse> logs, int page, int size,
                                long totalElements, int totalPages) {
        this.logs = logs;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<AuditLogResponse> getLogs() { return logs; }
    public void setLogs(List<AuditLogResponse> logs) { this.logs = logs; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
