package com.medicinelocator.reportservice.application.query;

import com.medicinelocator.reportservice.domain.enums.ReportStatus;

public class GetTicketQueueQuery {

    private final ReportStatus status;   // nullable -> all
    private final int page;
    private final int size;

    public GetTicketQueueQuery(ReportStatus status, int page, int size) {
        this.status = status;
        this.page = page;
        this.size = size;
    }

    public ReportStatus getStatus() { return status; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}
