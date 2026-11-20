package com.medicinelocator.prescription.domain.exception;

public class MalformedOcrResponseException extends RuntimeException {

    public MalformedOcrResponseException(String message) {
        super(message);
    }

    public MalformedOcrResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}