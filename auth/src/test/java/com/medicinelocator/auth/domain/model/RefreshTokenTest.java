package com.medicinelocator.auth.domain.model;

import com.medicinelocator.auth.domain.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RefreshToken domain model")
class RefreshTokenTest {

    private RefreshToken build(boolean revoked, boolean used, LocalDateTime expiresAt) {
        return new RefreshToken(
                UUID.randomUUID(),
                "token-hash",
                UUID.randomUUID(),
                Role.CUSTOMER,
                revoked,
                used,
                expiresAt,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("isExpired() returns false when expiresAt is in the future")
    void isExpired_falseWhenInFuture() {
        RefreshToken token = build(false, false, LocalDateTime.now().plusDays(1));
        assertFalse(token.isExpired());
    }

    @Test
    @DisplayName("isExpired() returns true when expiresAt has passed")
    void isExpired_trueWhenInPast() {
        RefreshToken token = build(false, false, LocalDateTime.now().minusMinutes(1));
        assertTrue(token.isExpired());
    }

    @Test
    @DisplayName("isValid() returns true only when token is fresh, not used, not revoked")
    void isValid_trueForFreshToken() {
        RefreshToken token = build(false, false, LocalDateTime.now().plusDays(7));
        assertTrue(token.isValid());
    }

    @Test
    @DisplayName("isValid() returns false when token is revoked")
    void isValid_falseWhenRevoked() {
        RefreshToken token = build(true, false, LocalDateTime.now().plusDays(7));
        assertFalse(token.isValid());
    }

    @Test
    @DisplayName("isValid() returns false when token is already used")
    void isValid_falseWhenUsed() {
        RefreshToken token = build(false, true, LocalDateTime.now().plusDays(7));
        assertFalse(token.isValid());
    }

    @Test
    @DisplayName("isValid() returns false when token is expired")
    void isValid_falseWhenExpired() {
        RefreshToken token = build(false, false, LocalDateTime.now().minusSeconds(1));
        assertFalse(token.isValid());
    }
}
