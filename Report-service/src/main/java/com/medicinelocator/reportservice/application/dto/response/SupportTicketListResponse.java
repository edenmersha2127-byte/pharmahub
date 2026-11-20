package com.medicinelocator.reportservice.application.dto.response;

import java.util.List;

/**
 * Paged list of support tickets (mirrors {@link ReportListResponse}).
 */
public class SupportTicketListResponse {

    private List<SupportTicketResponse> tickets;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public SupportTicketListResponse() {
    }

    public SupportTicketListResponse(List<SupportTicketResponse> tickets, int page, int size,
                                     long totalElements, int totalPages) {
        this.tickets = tickets;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<SupportTicketResponse> getTickets() { return tickets; }
    public void setTickets(List<SupportTicketResponse> tickets) { this.tickets = tickets; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
