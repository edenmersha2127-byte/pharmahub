package com.medicinelocator.auth.infrastructure.security;

import java.util.UUID;

/**
 * Principal object built from gateway-forwarded headers.
 * Set as the Authentication principal so controllers can access
 * the current admin's identity via SecurityContext if needed.
 */
public class GatewayAuthenticatedPrincipal {

    private final UUID userId;
    private final String email;
    private final String role;

    public GatewayAuthenticatedPrincipal(String userId, String email, String role) {
        this.userId = UUID.fromString(userId);
        this.email = email;
        this.role = role;
    }

    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getRole() { return role; }

    @Override
    public String toString() {
        return email != null ? email : userId.toString();
    }
}