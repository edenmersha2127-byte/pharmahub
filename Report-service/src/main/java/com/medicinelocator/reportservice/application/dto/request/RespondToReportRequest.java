package com.medicinelocator.reportservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * A pharmacy's reply to a report filed against it (right-to-respond).
 */
public class RespondToReportRequest {

    @NotBlank
    @Size(max = 2000)
    private String response;

    public RespondToReportRequest() {
    }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
}
