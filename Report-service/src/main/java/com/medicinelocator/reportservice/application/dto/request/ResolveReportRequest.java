package com.medicinelocator.reportservice.application.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Admin note attached when resolving or dismissing a report. Optional.
 */
public class ResolveReportRequest {

    @Size(max = 2000)
    private String note;

    public ResolveReportRequest() {
    }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
