package com.casestudy.insurancecampaign.gateway.openapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller that exposes aggregated OpenAPI definitions through API Gateway.
 *
 * <p>
 * This controller provides gateway-owned OpenAPI endpoints such as:
 * </p>
 *
 * <ul>
 *     <li>{@code /openapi/auth}</li>
 *     <li>{@code /openapi/campaign}</li>
 *     <li>{@code /openapi/dashboard}</li>
 *     <li>{@code /openapi/logs}</li>
 * </ul>
 *
 * <p>
 * It first tries to load the live OpenAPI document from the target microservice.
 * If the target service is unavailable, it returns a generated fallback OpenAPI document
 * so that Swagger UI can still display basic endpoint information.
 * </p>
 */
@RestController
@RequestMapping("/openapi")
public class GatewayOpenApiController {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final Map<String, OpenApiTarget> targets;

    /**
     * Creates a new OpenAPI aggregation controller.
     *
     * @param objectMapper        object mapper used to serialize fallback OpenAPI documents
     * @param authServiceUrl      Auth Service OpenAPI URL
     * @param campaignServiceUrl  Campaign Service OpenAPI URL
     * @param dashboardServiceUrl Dashboard Service OpenAPI URL
     * @param logServiceUrl       Log Service OpenAPI URL
     */
    public GatewayOpenApiController(
            WebClient.Builder loadBalancedWebClientBuilder,
            ObjectMapper objectMapper,
            @Value("${app.openapi.auth-service-url:lb://auth-service/v3/api-docs}") String authServiceUrl,
            @Value("${app.openapi.campaign-service-url:lb://campaign-service/v3/api-docs}") String campaignServiceUrl,
            @Value("${app.openapi.dashboard-service-url:lb://dashboard-service/v3/api-docs}") String dashboardServiceUrl,
            @Value("${app.openapi.log-service-url:lb://log-service/v3/api-docs}") String logServiceUrl
    ) {
        this.webClient = loadBalancedWebClientBuilder.build();
        this.objectMapper = objectMapper;
        this.targets = Map.of(
                "auth", new OpenApiTarget("Auth Service", authServiceUrl, authFallbackPaths()),
                "campaign", new OpenApiTarget("Campaign Service", campaignServiceUrl, campaignFallbackPaths()),
                "dashboard", new OpenApiTarget("Dashboard Service", dashboardServiceUrl, dashboardFallbackPaths()),
                "logs", new OpenApiTarget("Log Service", logServiceUrl, logFallbackPaths())
        );
    }

    /**
     * Returns the OpenAPI document for the requested service.
     *
     * <p>
     * If the target service exists and its OpenAPI endpoint is reachable, the live document
     * is returned. If the upstream call fails, a fallback OpenAPI document is generated.
     * </p>
     *
     * @param service OpenAPI target key, such as {@code auth}, {@code campaign},
     *                {@code dashboard}, or {@code logs}
     * @return OpenAPI JSON response
     */
    @GetMapping(value = "/{service}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> openApi(@PathVariable String service) {
        OpenApiTarget target = targets.get(service);
        if (target == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json(Map.of(
                            "code", "OPENAPI_TARGET_NOT_FOUND",
                            "message", "Unknown OpenAPI target: " + service
                    ))));
        }

        return webClient.get()
                .uri(target.upstreamUrl())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::jsonResponse)
                .onErrorResume(exception -> Mono.just(jsonResponse(fallbackDocument(target, exception))));
    }

    /**
     * Wraps a JSON body into an HTTP 200 response.
     *
     * @param body JSON response body
     * @return HTTP response with JSON content type
     */
    private ResponseEntity<String> jsonResponse(String body) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    /**
     * Creates a fallback OpenAPI document when a target service OpenAPI endpoint
     * cannot be loaded.
     *
     * @param target    OpenAPI target metadata
     * @param exception exception that caused fallback generation
     * @return fallback OpenAPI document as JSON
     */
    private String fallbackDocument(OpenApiTarget target, Throwable exception) {
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("openapi", "3.0.1");
        document.put("info", Map.of(
                "title", target.title() + " OpenAPI",
                "version", "1.0.0",
                "description", "Fallback OpenAPI document generated by api-gateway because the live service OpenAPI endpoint could not be loaded. Cause: " + exception.getClass().getSimpleName()
        ));
        document.put("servers", List.of(Map.of(
                "url", "http://localhost:8080",
                "description", "API Gateway"
        )));
        document.put("paths", target.fallbackPaths());
        document.put("components", Map.of(
                "securitySchemes", Map.of(
                        "bearerAuth", Map.of(
                                "type", "http",
                                "scheme", "bearer",
                                "bearerFormat", "JWT"
                        )
                )
        ));
        return json(document);
    }

    /**
     * Serializes the given map into JSON.
     *
     * @param value map value to serialize
     * @return JSON string, or minimal fallback OpenAPI JSON if serialization fails
     */
    private String json(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "{\"openapi\":\"3.0.1\",\"info\":{\"title\":\"OpenAPI fallback error\",\"version\":\"1.0.0\"},\"paths\":{}}";
        }
    }

    /**
     * Creates fallback OpenAPI paths for Auth Service.
     *
     * @return fallback path map for authentication endpoints
     */
    private static Map<String, Object> authFallbackPaths() {
        Map<String, Object> paths = new LinkedHashMap<>();
        paths.put("/api/v1/auth/register", path("post", "Register ROLE_USER or ROLE_ADMIN account", false));
        paths.put("/api/v1/auth/login", path("post", "Create JWT access token and refresh token", false));
        paths.put("/api/v1/auth/refresh-token", path("post", "Rotate refresh token and create a new access token", false));
        paths.put("/api/v1/auth/forgot-password", path("post", "Generate password reset token", false));
        paths.put("/api/v1/auth/reset-password", path("post", "Reset password using forgot-password token", false));
        paths.put("/api/v1/auth/logout", path("post", "Logout by revoking refresh token", false));
        return paths;
    }

    /**
     * Creates fallback OpenAPI paths for Campaign Service.
     *
     * @return fallback path map for campaign endpoints
     */
    private static Map<String, Object> campaignFallbackPaths() {
        Map<String, Object> paths = new LinkedHashMap<>();
        paths.put("/api/v1/campaigns", path("post", "Create insurance campaign. ROLE_ADMIN only", true, "get", "List campaigns with pagination and sorting. ROLE_USER and ROLE_ADMIN", true));
        paths.put("/api/v1/campaigns/{id}", path("get", "Get campaign by id. ROLE_USER and ROLE_ADMIN", true));
        paths.put("/api/v1/campaigns/{id}/approve", path("patch", "Approve PENDING_APPROVAL campaign. ROLE_ADMIN only", true));
        paths.put("/api/v1/campaigns/{id}/deactivate", path("patch", "Deactivate ACTIVE or PENDING_APPROVAL campaign. ROLE_ADMIN only", true));
        paths.put("/api/v1/campaigns/{id}/history", path("get", "List campaign status history. ROLE_ADMIN only", true));
        paths.put("/api/v1/campaigns/statistics", path("get", "Campaign statistics by status. ROLE_ADMIN only", true));
        return paths;
    }

    /**
     * Creates fallback OpenAPI paths for Dashboard Service.
     *
     * @return fallback path map for dashboard endpoints
     */
    private static Map<String, Object> dashboardFallbackPaths() {
        Map<String, Object> paths = new LinkedHashMap<>();
        paths.put("/api/v1/dashboard/classifieds/statistics", path("get", "Classified campaign statistics by status", true));
        return paths;
    }

    /**
     * Creates fallback OpenAPI paths for Log Service.
     *
     * @return fallback path map for log endpoints
     */
    private static Map<String, Object> logFallbackPaths() {
        Map<String, Object> paths = new LinkedHashMap<>();
        paths.put("/api/v1/logs/slow-requests", path("get", "List slow requests logged because they exceeded 5 ms", true));
        return paths;
    }

    /**
     * Creates a path item with a single HTTP operation.
     *
     * @param method  HTTP method
     * @param summary operation summary
     * @param secured whether the endpoint requires bearer authentication
     * @return OpenAPI path item map
     */
    private static Map<String, Object> path(String method, String summary, boolean secured) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put(method, operation(summary, secured));
        return item;
    }

    /**
     * Creates a path item with two HTTP operations.
     *
     * @param firstMethod   first HTTP method
     * @param firstSummary  first operation summary
     * @param firstSecured  whether the first operation requires authentication
     * @param secondMethod  second HTTP method
     * @param secondSummary second operation summary
     * @param secondSecured whether the second operation requires authentication
     * @return OpenAPI path item map
     */
    private static Map<String, Object> path(
            String firstMethod,
            String firstSummary,
            boolean firstSecured,
            String secondMethod,
            String secondSummary,
            boolean secondSecured
    ) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put(firstMethod, operation(firstSummary, firstSecured));
        item.put(secondMethod, operation(secondSummary, secondSecured));
        return item;
    }

    /**
     * Creates an OpenAPI operation object.
     *
     * @param summary operation summary
     * @param secured whether the operation requires bearer authentication
     * @return OpenAPI operation map
     */
    private static Map<String, Object> operation(String summary, boolean secured) {
        Map<String, Object> operation = new LinkedHashMap<>();
        operation.put("summary", summary);
        operation.put("responses", Map.of(
                "200", Map.of("description", "OK"),
                "400", Map.of("description", "Bad Request"),
                "401", Map.of("description", "Unauthorized"),
                "403", Map.of("description", "Forbidden"),
                "409", Map.of("description", "Conflict / business rule violation"),
                "500", Map.of("description", "Internal Server Error")
        ));
        if (secured) {
            operation.put("security", List.of(Map.of("bearerAuth", List.of())));
        }
        return operation;
    }

    /**
     * Metadata for a service OpenAPI target.
     *
     * @param title         display title of the target service
     * @param upstreamUrl   live OpenAPI endpoint URL of the target service
     * @param fallbackPaths fallback path definitions used when the live endpoint is unavailable
     */
    private record OpenApiTarget(String title, String upstreamUrl, Map<String, Object> fallbackPaths) {
    }
}
