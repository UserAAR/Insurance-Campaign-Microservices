package com.casestudy.insurancecampaign.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Servlet filter responsible for extracting JWT access tokens from the
 * {@code Authorization} header and registering the authenticated user
 * in the Spring Security context.
 *
 * <p>
 * Expected header format:
 * </p>
 *
 * <pre>
 * Authorization: Bearer &lt;access-token&gt;
 * </pre>
 *
 * <p>
 * The filter parses JWT claims through {@link JwtTokenService}, extracts
 * user roles from the {@code roles} claim, converts them into
 * {@link SimpleGrantedAuthority} objects, and creates a
 * {@link UsernamePasswordAuthenticationToken}.
 * </p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    /**
     * Creates a new JWT authentication filter.
     *
     * @param jwtTokenService service used to parse and validate JWT tokens
     */
    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * Extracts the bearer token from the request, validates it, and sets
     * the authenticated user into {@link SecurityContextHolder}.
     *
     * <p>
     * If the request does not contain a bearer token, the filter simply
     * continues the filter chain without creating an authentication object.
     * </p>
     *
     * @param request     current HTTP request
     * @param response    current HTTP response
     * @param filterChain remaining filter chain
     * @throws ServletException if the filter chain fails with a servlet error
     * @throws IOException      if the filter chain fails with an I/O error
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            Claims claims = jwtTokenService.parseClaims(token);
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            Collection<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

}
