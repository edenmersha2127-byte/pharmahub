package com.medicinelocator.inventory.infrastructure.security;

import com.medicinelocator.inventory.domain.exception.AccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GatewayHeaderExtractor {

    private static final Logger log = LoggerFactory.getLogger(GatewayHeaderExtractor.class);

    private static final String HEADER_USER_ID          = "X-User-Id";
    private static final String HEADER_USER_ROLE        = "X-User-Role";
    private static final String HEADER_USER_EMAIL       = "X-User-Email";
    private static final String HEADER_PHARMACY_ID      = "X-Pharmacy-Id";
    private static final String HEADER_USER_PERMISSIONS = "X-User-Permissions";

    public CurrentUser extract(HttpServletRequest request) {
        String userIdStr   = request.getHeader(HEADER_USER_ID);
        String role        = request.getHeader(HEADER_USER_ROLE);
        String email       = request.getHeader(HEADER_USER_EMAIL);
        String pharmacyStr = request.getHeader(HEADER_PHARMACY_ID);
        String permsStr    = request.getHeader(HEADER_USER_PERMISSIONS);

        if (userIdStr == null || userIdStr.isBlank()) {
            throw new AccessDeniedException("Missing X-User-Id header. Request must pass through the API Gateway.");
        }

        try {
            UUID userId = UUID.fromString(userIdStr);
            UUID pharmacyId = parsePharmacyId(pharmacyStr);
            Set<String> permissions = parsePermissions(permsStr);
            return new CurrentUser(
                    userId,
                    role != null ? role : "",
                    email != null ? email : "",
                    pharmacyId,
                    permissions);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid X-User-Id header value: {}", userIdStr);
            throw new AccessDeniedException("Invalid identity header received from gateway");
        }
    }

    private UUID parsePharmacyId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid X-Pharmacy-Id header value: {}", value);
            return null;
        }
    }

    private Set<String> parsePermissions(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
