package com.medicinelocator.auth.application.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for a pharmacy toggling its open/close status.
 */
public class SetOpenStatusRequest {

    @NotNull(message = "open is required")
    private Boolean open;

    public SetOpenStatusRequest() {
    }

    public Boolean getOpen() { return open; }
    public void setOpen(Boolean open) { this.open = open; }
}
