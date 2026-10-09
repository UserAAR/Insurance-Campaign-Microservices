package com.casestudy.insurancecampaign.gateway.filter;

import com.casestudy.insurancecampaign.gateway.security.GatewayJwtTokenService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Global API Gateway filter responsible for validating JWT access tokens.
 *
 * <p>
 * This filter protects gateway routes before requests are forwarded to downstream
 * services. Public paths such as authentication endpoints, Swagger/OpenAPI endpoints,
 * and Actuator endpoints are allowed without authentication.
 * </p>
 *
 * <p>
 * For protected paths, the filter expects the following header:
 * </p>
 *
 * <pre>
 * Authorization: Bearer &lt;access-token&gt;
 * </pre>
 *
 * <p>
 * If the token is missing or invalid, the gateway returns {@code 401 Unauthorized}.
 * </p>
 */
@Component
public class GatewayJwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final List<String> OPEN_PATHS = List.of(
            "/api/v1/auth",
            "/actuator",
            "/swagger-ui",
            "/swagger-ui.html",
            "/v3/api-docs",
            "/openapi",
            "/auth/v3/api-docs",
            "/campaign/v3/api-docs",
            "/dashboard/v3/api-docs",
            "/logs/v3/api-docs"
    );

    private final GatewayJwtTokenService jwtTokenService;

    /**
     * Creates a new gateway JWT authentication filter.
     *
     * @param jwtTokenService service used to parse and validate JWT tokens
     */
    public GatewayJwtAuthenticationFilter(GatewayJwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * Filters incoming gateway requests and validates JWT tokens for protected paths.
     *
     * <p>
     * Requests matching {@link #OPEN_PATHS} are forwarded without token validation.
     * All other requests must contain a valid bearer token.
     * </p>
     *
     * @param exchange current server web exchange
     * @param chain    gateway filter chain
     * @return completion signal for the reactive filter chain
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (OPEN_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            jwtTokenService.parseClaims(authorization.substring(7));
            return chain.filter(exchange);
        } catch (RuntimeException exception) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    /**
     * Defines the execution order of this gateway filter.
     *
     * <p>
     * This filter runs after the RequestRateLimiter so repeated requests can be
     * blocked with {@code 429 Too Many Requests} before JWT validation returns
     * {@code 401 Unauthorized}.
     * </p>
     *
     * @return filter order
     */
    @Override
    public int getOrder() {
        return 100;
    }

}
