package com.medicinelocator.gateway.filter;

import com.medicinelocator.gateway.exception.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
    private static final String BEARER_PREFIX = "Bearer ";
    // How long the client should wait before retrying, in seconds. Matches the
    // 1-minute counting window below.
    private static final String RETRY_AFTER_SECONDS = "60";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final String jwtSecret;
    private final int maxRequestsPerMinute;

    public RateLimitFilter(ReactiveStringRedisTemplate redisTemplate,
                           ObjectMapper objectMapper,
                           @Value("${jwt.secret}") String jwtSecret,
                           @Value("${rate-limit.requests-per-minute:600}") int maxRequestsPerMinute) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.jwtSecret = jwtSecret;
        this.maxRequestsPerMinute = maxRequestsPerMinute;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // CORS preflight requests are sent automatically by the browser and must
        // never be throttled, or the real request that follows is blocked too.
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // Prefer a per-user bucket so many users sharing one IP (office network,
        // mobile carrier, load balancer) don't drain a single shared quota. Falls
        // back to per-IP for anonymous or untrusted requests.
        String redisKey = RATE_LIMIT_KEY_PREFIX + resolveClientKey(exchange);

        return redisTemplate.opsForValue()
                .increment(redisKey)
                .flatMap(count -> {
                    if (count == 1) {
                        return redisTemplate.expire(redisKey, Duration.ofMinutes(1))
                                .then(Mono.just(count));
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    if (count > maxRequestsPerMinute) {
                        log.warn("Rate limit exceeded for key: {}", redisKey);
                        return tooManyRequestsResponse(exchange);
                    }
                    exchange.getResponse().getHeaders().set("X-RateLimit-Remaining",
                            String.valueOf(maxRequestsPerMinute - count));
                    return chain.filter(exchange);
                })
                // Fail OPEN: the rate limiter must never be a hard dependency. If
                // Redis is unreachable, allow the request through (and log it)
                // rather than returning an error to every user.
                .onErrorResume(error -> {
                    log.error("Rate limiter unavailable, allowing request through: {}",
                            error.getMessage());
                    return chain.filter(exchange);
                });
    }

    /**
     * Returns "user:{id}" for a request carrying a valid JWT, otherwise
     * "ip:{clientIp}". The token is cryptographically verified so a forged token
     * cannot mint arbitrary user buckets to dodge the limit — an invalid token
     * simply falls back to the per-IP bucket.
     */
    private String resolveClientKey(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String userId = extractUserId(authHeader.substring(BEARER_PREFIX.length()));
            if (userId != null && !userId.isBlank()) {
                return "user:" + userId;
            }
        }
        return "ip:" + resolveClientIp(exchange);
    }

    private String extractUserId(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            // Expired / invalid / malformed token: treat as anonymous (per-IP).
            return null;
        }
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    private Mono<Void> tooManyRequestsResponse(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        // Tell the client exactly how long to wait so it can back off gracefully
        // instead of showing an error or hammering the gateway harder.
        response.getHeaders().set(HttpHeaders.RETRY_AFTER, RETRY_AFTER_SECONDS);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Too Many Requests",
                "Rate limit exceeded. Please try again in a minute.",
                exchange.getRequest().getPath().value(),
                LocalDateTime.now().toString()
        );

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException e) {
            bytes = "{\"error\":\"Too Many Requests\"}".getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
