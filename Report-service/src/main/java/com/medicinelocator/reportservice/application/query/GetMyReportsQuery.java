package com.medicinelocator.reportservice.application.query;

import java.util.UUID;

public class GetMyReportsQuery {

    private final UUID reporterId;
    private final int page;
    private final int size;

    public GetMyReportsQuery(UUID reporterId, int page, int size) {
        this.reporterId = reporterId;
        this.page = page;
        this.size = size;
    }

    public UUID getReporterId() { return reporterId; }
    public int getPage() { return page; }
    public int getSize() { return size; }
}
