package com.casestudy.insurancecampaign.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.casestudy.insurancecampaign.common.api.ApiError;
import com.casestudy.insurancecampaign.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * Spring Security configuration for protected servlet-based microservices.
 *
 * <p>
 * This configuration enables stateless JWT-based authentication and method-level
 * authorization with {@link EnableMethodSecurity}.
 * </p>
 *
 * <p>
 * Public endpoints such as authentication APIs, actuator endpoints, Swagger UI,
 * OpenAPI docs, and gRPC health endpoints are permitted without authentication.
 * All other endpoints require a valid JWT token.
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ServiceSecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new service security configuration.
     *
     * @param jwtAuthenticationFilter JWT filter used to authenticate incoming requests
     * @param objectMapper            object mapper used to serialize error responses
     */
    public ServiceSecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    /**
     * Defines the servlet security filter chain.
     *
     * <p>
     * Security behavior:
     * </p>
     *
     * <ul>
     *     <li>CSRF is disabled because services are stateless REST APIs.</li>
     *     <li>Session creation is disabled with {@link SessionCreationPolicy#STATELESS}.</li>
     *     <li>Unauthorized requests return a common {@link ApiError} response.</li>
     *     <li>Forbidden requests return a common {@link ApiError} response.</li>
     *     <li>Public endpoints are permitted without authentication.</li>
     *     <li>All other endpoints require authentication.</li>
     *     <li>{@link JwtAuthenticationFilter} is registered before {@link UsernamePasswordAuthenticationFilter}.</li>
     * </ul>
     *
     * @param http Spring Security HTTP configuration
     * @return configured security filter chain
     * @throws Exception if the security filter chain cannot be built
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> writeError(
                                request,
                                response,
                                HttpStatus.UNAUTHORIZED,
                                ErrorCode.UNAUTHORIZED.name(),
                                "Authentication is required"
                        ))
                        .accessDeniedHandler((request, response, accessDeniedException) -> writeError(
                                request,
                                response,
                                HttpStatus.FORBIDDEN,
                                ErrorCode.FORBIDDEN.name(),
                                "Access denied"
                        ))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/actuator/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/grpc.health.v1.Health/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Writes a common JSON error response for authentication and authorization failures.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @param status   HTTP status to return
     * @param code     application-level error code
     * @param message  human-readable error message
     * @throws IOException if response writing fails
     */
    private void writeError(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getWriter(),
                ApiError.of(code, message, status.value(), request.getRequestURI())
        );
    }

}
