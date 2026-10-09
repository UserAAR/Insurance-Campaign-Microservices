package com.casestudy.insurancecampaign.common.web;

import com.casestudy.insurancecampaign.common.event.SlowRequestLogEvent;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Servlet filter that detects and publishes slow HTTP requests.
 *
 * <p>If a request takes longer than the configured threshold, this filter creates
 * a {@link SlowRequestLogEvent} and publishes it to Kafka. The log-service can
 * then consume the event and store it in Elasticsearch.</p>
 *
 * <p>The threshold, Kafka topic name, and service name are configurable through
 * application properties.</p>
 */
@Component
@ConditionalOnClass(name = "org.springframework.kafka.core.KafkaTemplate")
public class SlowRequestLoggingFilter extends OncePerRequestFilter {

    private final ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider;
    private final long thresholdMs;
    private final String topicName;
    private final String applicationName;

    /**
     * Creates a slow request logging filter.
     *
     * @param kafkaTemplateProvider provider for Kafka template; allows the filter to work even if Kafka is unavailable
     * @param thresholdMs           request duration threshold in milliseconds
     * @param topicName             Kafka topic where slow request events will be published
     * @param applicationName       current Spring application name
     */
    public SlowRequestLoggingFilter(
            ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider,
            @Value("${app.slow-request-threshold-ms:5}") long thresholdMs,
            @Value("${app.kafka.topics.log-events:log-events}") String topicName,
            @Value("${spring.application.name:unknown-service}") String applicationName
    ) {
        this.kafkaTemplateProvider = kafkaTemplateProvider;
        this.thresholdMs = thresholdMs;
        this.topicName = topicName;
        this.applicationName = applicationName;
    }

    /**
     * Excludes monitoring and API-documentation endpoints from slow-request processing.
     * These endpoints must remain lightweight and must never trigger an external Kafka call.
     *
     * @param request current HTTP request
     * @return {@code true} when this filter should be skipped
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/openapi");
    }

    /**
     * Measures request execution time and publishes a slow request event when needed.
     *
     * <p>The filter collects basic request information such as method, path,
     * status code, duration, trace ID, user agent, and forwarded IP address.</p>
     *
     * @param request     current HTTP request
     * @param response    current HTTP response
     * @param filterChain servlet filter chain
     * @throws ServletException if the downstream filter or servlet fails
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.nanoTime();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000;

            if (durationMs > thresholdMs) {
                SlowRequestLogEvent event = new SlowRequestLogEvent(
                        applicationName,
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        durationMs,
                        Optional.ofNullable(MDC.get("traceId")).orElse("N/A"),
                        Map.of(
                                "user-agent", Optional.ofNullable(request.getHeader("User-Agent")).orElse("N/A"),
                                "x-forwarded-for", Optional.ofNullable(request.getHeader("X-Forwarded-For")).orElse("N/A")
                        ),
                        Instant.now()
                );

                KafkaTemplate<String, Object> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();

                if (kafkaTemplate != null) {
                    kafkaTemplate.send(topicName, request.getRequestURI(), event);
                }
            }
        }
    }

}