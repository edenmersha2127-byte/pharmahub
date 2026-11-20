package com.medicinelocator.gateway.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ErrorResponse value object")
class ErrorResponseTest {

    @Test
    @DisplayName("constructor stores all five fields in correct getters")
    void constructor_storesAllFields() {
        ErrorResponse response = new ErrorResponse(
                404,
                "Not Found",
                "Resource missing",
                "/api/missing",
                "2026-06-24T18:00:00"
        );

        assertEquals(404, response.getStatus());
        assertEquals("Not Found", response.getError());
        assertEquals("Resource missing", response.getMessage());
        assertEquals("/api/missing", response.getPath());
        assertEquals("2026-06-24T18:00:00", response.getTimestamp());
    }

    @Test
    @DisplayName("handles null message gracefully")
    void constructor_handlesNullMessage() {
        ErrorResponse response = new ErrorResponse(500, "Internal Server Error",
                null, "/api/x", "2026-06-24T18:00:00");

        assertNull(response.getMessage());
        assertEquals(500, response.getStatus());
    }

    @Test
    @DisplayName("supports common HTTP status codes")
    void constructor_acceptsCommonStatuses() {
        int[] statuses = {200, 201, 400, 401, 403, 404, 500, 502, 503};
        for (int status : statuses) {
            ErrorResponse response = new ErrorResponse(status, "msg", "x", "/", "t");
            assertEquals(status, response.getStatus());
        }
    }
}
