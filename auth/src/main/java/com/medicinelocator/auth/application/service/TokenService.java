package com.medicinelocator.auth.application.service;

import com.medicinelocator.auth.domain.enums.Role;
import com.medicinelocator.auth.domain.model.RefreshToken;

import java.util.List;
import java.util.UUID;

public interface TokenService {

    String generateAccessToken(UUID userId, String email, Role role);

    /**
     * Extended access token for pharmacy-scoped accounts (PHARMACY owner and
     * PHARMACY_STAFF). Embeds the effective pharmacyId and the granted
     * permissions so downstream services can authorise without calling back.
     * A null/empty permission list means "unrestricted" (owner).
     */
    String generateAccessToken(UUID userId, String email, Role role,
                               UUID pharmacyId, List<String> permissions);

    RefreshToken generateRefreshToken(UUID userId, Role role);

    RefreshToken findRefreshToken(String rawToken);

    void revokeRefreshToken(String tokenHash);

    void revokeAllUserRefreshTokens(UUID userId, Role role);

    void blacklistAccessToken(String accessToken, long remainingTtlMillis);

    boolean isAccessTokenBlacklisted(String accessToken);

    long getAccessTokenRemainingTtl(String accessToken);
}