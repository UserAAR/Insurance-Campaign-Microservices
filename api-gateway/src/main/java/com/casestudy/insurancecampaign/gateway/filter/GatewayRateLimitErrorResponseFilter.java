package com.casestudy.insurancecampaign.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Writes a standardized JSON error response when API Gateway rate limiting rejects a request.
 *
 * <p>
 * Spring Cloud Gateway's {@code RequestRateLimiter} returns {@code 429 Too Many Requests}
 * with an empty body by default. This filter decorates the response and converts that empty
 * response into the same API error style used by the downstream services.
 * </p>
 */
@Component
public class GatewayRateLimitErrorResponseFilter implements GlobalFilter, Ordered {

    private static final String ERROR_CODE = "TOO_MANY_REQUESTS";
    private static final String ERROR_MESSAGE = "Too many requests. Please wait 5 seconds before sending another request.";
    private static final String RETRY_AFTER_SECONDS = "5";

    private final ObjectMapper objectMapper;

    /**
     * Creates a new rate limit error response filter.
     *
     * @param objectMapper object mapper used to serialize the error response
     */
    public GatewayRateLimitErrorResponseFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Decorates the gateway response so rate limit rejections return a JSON body.
     *
     * @param exchange current server web exchange
     * @param chain    gateway filter chain
     * @return completion signal for the reactive filter chain
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> setComplete() {
                if (HttpStatus.TOO_MANY_REQUESTS.equals(getStatusCode()) && !isCommitted()) {
                    return writeRateLimitError(exchange, this);
                }

                return super.setComplete();
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }

    private Mono<Void> writeRateLimitError(
            ServerWebExchange exchange,
            ServerHttpResponseDecorator response
    ) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(HttpHeaders.RETRY_AFTER, RETRY_AFTER_SECONDS);

        GatewayApiError error = new GatewayApiError(
                ERROR_CODE,
                ERROR_MESSAGE,
                HttpStatus.TOO_MANY_REQUESTS.value(),
                exchange.getRequest().getURI().getPath(),
                Map.of(),
                Instant.now()
        );

        byte[] body = serialize(error);
        DataBuffer buffer = response.bufferFactory().wrap(body);

        return response.writeWith(Mono.just(buffer));
    }

    private byte[] serialize(GatewayApiError error) {
        try {
            return objectMapper.writeValueAsBytes(error);
        } catch (JsonProcessingException exception) {
            return fallbackBody(error).getBytes();
        }
    }

    private String fallbackBody(GatewayApiError error) {
        return "{"
                + "\"code\":\"" + error.code() + "\","
                + "\"message\":\"" + error.message() + "\","
                + "\"status\":" + error.status() + ","
                + "\"path\":\"" + error.path() + "\","
                + "\"validationErrors\":{},"
                + "\"timestamp\":\"" + error.timestamp() + "\""
                + "}";
    }

    /**
     * Runs before the built-in RequestRateLimiter so the response can be decorated first.
     *
     * @return filter order
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private record GatewayApiError(
            String code,
            String message,
            int status,
            String path,
            Map<String, String> validationErrors,
            Instant timestamp
    ) {
    }

}
