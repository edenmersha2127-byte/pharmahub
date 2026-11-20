package com.medicinelocator.reportservice.application.query;

public class GetAuditLogsQuery {

    private final int page;
    private final int size;

    public GetAuditLogsQuery(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public int getPage() { return page; }
    public int getSize() { return size; }
}
