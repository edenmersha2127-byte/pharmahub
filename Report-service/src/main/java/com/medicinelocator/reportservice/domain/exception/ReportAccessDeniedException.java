package com.medicinelocator.reportservice.domain.exception;

/**
 * Thrown when a caller tries to act on a report they do not own
 * (e.g. a pharmacy responding to a report filed against a different pharmacy).
 */
public class ReportAccessDeniedException extends RuntimeException {

    public ReportAccessDeniedException(String message) {
        super(message);
    }
}
