package com.medicinelocator.reportservice.application.dto.request;

import com.medicinelocator.reportservice.domain.enums.IssueCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload any authenticated user sends to raise a support ticket. The reporter's
 * identity (id + role) comes from the gateway X-User-Id / X-User-Role headers,
 * not the body.
 */
public class SubmitTicketRequest {

    @NotNull
    private IssueCategory category;

    @NotBlank
    @Size(max = 200)
    private String subject;

    @NotBlank
    @Size(max = 2000)
    private String description;

    public SubmitTicketRequest() {
    }

    public IssueCategory getCategory() { return category; }
    public void setCategory(IssueCategory category) { this.category = category; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
