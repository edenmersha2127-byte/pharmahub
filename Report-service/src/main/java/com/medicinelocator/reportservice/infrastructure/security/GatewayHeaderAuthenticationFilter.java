package com.medicinelocator.reportservice.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Reads the identity headers forwarded by the API Gateway (X-User-Id,
 * X-User-Email, X-User-Role) and builds a Spring Security Authentication from
 * them. The JWT is validated by the Gateway, never here.
 *
 * When the headers are absent (routes not passed through the gateway's JwtAuth
 * filter) the SecurityContext is left empty and the SecurityConfig rules apply.
 */
@Component
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(GatewayHeaderAuthenticationFilter.class);

    private static final String HEADER_USER_ID    = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLE  = "X-User-Role";
    private static final String ROLE_PREFIX       = "ROLE_";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader(HEADER_USER_ID);
        String email  = request.getHeader(HEADER_USER_EMAIL);
        String role   = request.getHeader(HEADER_USER_ROLE);

        if (userId != null && !userId.isBlank() && role != null && !role.isBlank()) {
            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority(ROLE_PREFIX + role)
            );

            GatewayAuthenticatedPrincipal principal =
                    new GatewayAuthenticatedPrincipal(userId, email, role);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("SecurityContext populated from gateway headers: userId={} role={}",
                    userId, role);
        }

        filterChain.doFilter(request, response);
    }
}
