package com.casestudy.insurancecampaign.gateway.config;

import com.casestudy.insurancecampaign.gateway.security.GatewayJwtTokenService;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * RateLimiter configuration for API Gateway.
 *
 * <p>
 * Authenticated requests are rate limited by JWT subject.
 * Public, unauthenticated, or invalid-token requests are rate limited by client IP.
 * </p>
 */
@Configuration
public class GatewayRateLimiterConfiguration {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";
    private static final String UNKNOWN_IP = "unknown";

    /**
     * Resolves RateLimiter key for each request.
     *
     * @param jwtTokenService JWT parser used to extract authenticated username
     * @return rate limiter key resolver
     */
    @Bean
    public KeyResolver requestRateLimiterKeyResolver(GatewayJwtTokenService jwtTokenService) {
        return exchange -> Mono.just(resolveKey(exchange, jwtTokenService));
    }

    private String resolveKey(ServerWebExchange exchange, GatewayJwtTokenService jwtTokenService) {
        String authorization = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            String token = authorization.substring(BEARER_PREFIX.length());

            try {
                Claims claims = jwtTokenService.parseClaims(token);
                String subject = claims.getSubject();

                if (subject != null && !subject.isBlank()) {
                    return "user:" + subject;
                }
            } catch (RuntimeException exception) {
                return "ip:" + resolveClientIp(exchange);
            }
        }

        return "ip:" + resolveClientIp(exchange);
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest()
                .getHeaders()
                .getFirst(X_FORWARDED_FOR);

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = exchange.getRequest()
                .getHeaders()
                .getFirst(X_REAL_IP);

        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();

        if (remoteAddress == null) {
            return UNKNOWN_IP;
        }

        if (remoteAddress.getAddress() != null) {
            return remoteAddress.getAddress().getHostAddress();
        }

        return remoteAddress.getHostString() == null ? UNKNOWN_IP : remoteAddress.getHostString();
    }
}