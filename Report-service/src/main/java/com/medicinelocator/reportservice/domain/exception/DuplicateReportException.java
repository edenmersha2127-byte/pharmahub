package com.medicinelocator.reportservice.domain.exception;

/**
 * Thrown when a customer tries to report the same pharmacy again within the
 * configured cooldown window (anti-spam / anti-abuse).
 */
public class DuplicateReportException extends RuntimeException {

    public DuplicateReportException(String message) {
        super(message);
    }
}
