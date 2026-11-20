package com.medicinelocator.salesservice.presentation.exception;

import com.medicinelocator.salesservice.domain.exception.AccessDeniedException;
import com.medicinelocator.salesservice.domain.exception.CheckoutFailedException;
import com.medicinelocator.salesservice.domain.exception.PharmacyCustomerNotFoundException;
import com.medicinelocator.salesservice.domain.exception.SaleNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(), "Validation Failed",
                "One or more fields have validation errors",
                request.getRequestURI(), LocalDateTime.now(), fieldErrors));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorDetails(
                HttpStatus.FORBIDDEN.value(), "Access Denied", ex.getMessage(),
                request.getRequestURI(), LocalDateTime.now(), null));
    }

    @ExceptionHandler({PharmacyCustomerNotFoundException.class, SaleNotFoundException.class})
    public ResponseEntity<ErrorDetails> handleNotFound(
            RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDetails(
                HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(),
                request.getRequestURI(), LocalDateTime.now(), null));
    }

    @ExceptionHandler(CheckoutFailedException.class)
    public ResponseEntity<ErrorDetails> handleCheckoutFailed(
            CheckoutFailedException ex, HttpServletRequest request) {
        HttpStatus status = ex.isClientError() ? HttpStatus.CONFLICT : HttpStatus.BAD_GATEWAY;
        return ResponseEntity.status(status).body(new ErrorDetails(
                status.value(), "Checkout Failed", ex.getMessage(),
                request.getRequestURI(), LocalDateTime.now(), null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDetails(
                HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage(),
                request.getRequestURI(), LocalDateTime.now(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorDetails(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI(), LocalDateTime.now(), null));
    }

    public record ErrorDetails(
            int status,
            String error,
            String message,
            String path,
            LocalDateTime timestamp,
            Map<String, String> fieldErrors
    ) {}
}
