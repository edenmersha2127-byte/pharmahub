package com.medicinelocator.gateway.filter;

import com.medicinelocator.gateway.exception.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

@Component
public class JwtAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthGatewayFilterFactory.class);

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final String HEADER_PHARMACY_ID = "X-Pharmacy-Id";
    private static final String HEADER_USER_PERMISSIONS = "X-User-Permissions";

    private final String jwtSecret;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public JwtAuthGatewayFilterFactory(
            @Value("${jwt.secret}") String jwtSecret,
            ReactiveStringRedisTemplate redisTemplate,
            ObjectMapper objectMapper
    ) {
        super(Config.class);
        this.jwtSecret = jwtSecret;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                return unauthorized(exchange, "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(BEARER_PREFIX.length());

            return redisTemplate.hasKey("blacklist:" + token)
                    .flatMap(isBlacklisted -> {

                        if (Boolean.TRUE.equals(isBlacklisted)) {
                            return unauthorized(exchange, "Token has been revoked");
                        }

                        try {
                            Claims claims = parseToken(token);

                            String pharmacyId = claims.get("pharmacyId", String.class);
                            String permissionsCsv = extractPermissions(claims);

                            // Overwrite (set) identity headers so a client can never
                            // spoof them — only the gateway-derived values survive.
                            ServerHttpRequest mutatedRequest = exchange.getRequest()
                                    .mutate()
                                    .headers(h -> {
                                        h.set(HEADER_USER_ID, claims.getSubject() != null ? claims.getSubject() : "");
                                        h.set(HEADER_USER_EMAIL, claims.get("email", String.class) != null ? claims.get("email", String.class) : "");
                                        h.set(HEADER_USER_ROLE, claims.get("role", String.class) != null ? claims.get("role", String.class) : "");
                                        h.set(HEADER_PHARMACY_ID, pharmacyId != null ? pharmacyId : "");
                                        h.set(HEADER_USER_PERMISSIONS, permissionsCsv);
                                    })
                                    .build();

                            return chain.filter(exchange.mutate().request(mutatedRequest).build());

                        } catch (ExpiredJwtException e) {
                            log.warn("JWT expired: {}", e.getMessage());
                            return unauthorized(exchange, "Token has expired");

                        } catch (JwtException e) {
                            log.warn("JWT invalid: {}", e.getMessage());
                            return unauthorized(exchange, "Invalid token");
                        }
                    });
        };
    }

    /**
     * The "permissions" claim is a JSON array of permission names. Flatten it to
     * a comma-separated header value; empty string when absent (owner tokens omit
     * it, meaning "unrestricted" — downstream treats a PHARMACY role as all-access).
     */
    private String extractPermissions(Claims claims) {
        Object raw = claims.get("permissions");
        if (raw instanceof java.util.List<?> list) {
            return list.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
        }
        return "";
    }

    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(
                Base64.getDecoder().decode(jwtSecret)
        );

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                message,
                exchange.getRequest().getPath().value(),
                LocalDateTime.now().toString()
        );

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(error);
        } catch (JsonProcessingException e) {
            bytes = "{\"error\":\"Unauthorized\"}".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // empty config for GatewayFilterFactory
    }
}