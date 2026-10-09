package com.casestudy.insurancecampaign.auth.client;

import com.casestudy.insurancecampaign.auth.model.domain.AuthAuditLog;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * HTTP exchange client used by auth-service to send audit events to log-service.
 */
@HttpExchange("/api/v1/logs")
public interface LogServiceHttpClient {

    /**
     * Sends an authentication audit log to log-service.
     *
     * <p>
     * This external HTTP call is protected with Resilience4j CircuitBreaker,
     * Retry and Bulkhead so log-service failures do not break auth-service flows.
     * </p>
     *
     * @param request authentication audit log payload
     */
    @PostExchange("/auth-audit")
    @CircuitBreaker(name = "logServiceHttpClient")
    @Retry(name = "logServiceHttpClient")
    @Bulkhead(name = "logServiceHttpClient", type = Bulkhead.Type.SEMAPHORE)
    void sendAuthAuditLog(@RequestBody AuthAuditLog request);

}