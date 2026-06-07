package in.healix.security.jwt;

import in.healix.core.tenant.TenantContext;
import in.healix.security.auth.HealixUserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    // In production, load from vault or properties. Using standard high-entropy key for validation.
    private final String secretString = "healix-secure-high-entropy-jwt-secret-key-32bytes-long";
    private final SecretKey key = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(jwt)
                        .getPayload();

                String email = claims.getSubject();
                UUID tenantId = UUID.fromString(claims.get("tenant_id", String.class));
                UUID userId = UUID.fromString(claims.get("user_id", String.class));
                
                java.util.List<?> rolesList = claims.get("roles", java.util.List.class);
                java.util.List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = 
                    rolesList == null ? java.util.Collections.emptyList() :
                    rolesList.stream()
                        .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(role.toString()))
                        .collect(java.util.stream.Collectors.toList());

                HealixUserPrincipal principal = new HealixUserPrincipal(
                        email, "", tenantId, userId, authorities
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // Enforce tenant context from authenticated JWT only
                TenantContext.setTenantId(tenantId.toString());

            } catch (Exception ex) {
                log.warn("JWT validation failed: {}", ex.getMessage());
                SecurityContextHolder.clearContext();
                TenantContext.clear();
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Guarantee clear context on thread release (critical for threadpools / Loom)
            TenantContext.clear();
        }
    }
}
