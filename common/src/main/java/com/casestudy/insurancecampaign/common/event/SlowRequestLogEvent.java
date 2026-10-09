package com.casestudy.insurancecampaign.common.event;

import java.time.Instant;
import java.util.Map;

/**
 * Event representing a slow HTTP request detected by a service.
 *
 * <p>This event is published when a request execution time exceeds the configured
 * slow request threshold. It can be consumed by log-service and stored in
 * Elasticsearch for later analysis.</p>
 *
 * @param serviceName service where the slow request occurred
 * @param method      HTTP method
 * @param path        request path
 * @param status      HTTP response status code
 * @param durationMs  request duration in milliseconds
 * @param traceId     distributed tracing trace identifier
 * @param headers     selected request headers useful for debugging
 * @param occurredAt  time when the slow request was detected
 */
public record SlowRequestLogEvent(
        String serviceName,
        String method,
        String path,
        int status,
        long durationMs,
        String traceId,
        Map<String, String> headers,
        Instant occurredAt
) { }
