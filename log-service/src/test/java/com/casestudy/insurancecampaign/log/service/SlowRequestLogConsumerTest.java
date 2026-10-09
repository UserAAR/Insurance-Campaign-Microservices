package com.casestudy.insurancecampaign.log.service;

import com.casestudy.insurancecampaign.common.event.SlowRequestLogEvent;
import com.casestudy.insurancecampaign.log.base.AbstractBaseServiceTest;
import com.casestudy.insurancecampaign.log.model.document.SlowRequestDocument;
import com.casestudy.insurancecampaign.log.repository.SlowRequestLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

class SlowRequestLogConsumerTest extends AbstractBaseServiceTest {

    @Mock
    private SlowRequestLogRepository slowRequestLogRepository;

    private SlowRequestLogConsumer slowRequestLogConsumer;

    @BeforeEach
    void setUp() {
        slowRequestLogConsumer = new SlowRequestLogConsumer(slowRequestLogRepository);
    }

    @Test
    void shouldConsumeSlowRequestLogEventAndSaveDocument() {
        // Given
        Instant occurredAt = Instant.parse("2026-07-26T12:15:00Z");

        SlowRequestLogEvent event = new SlowRequestLogEvent(
                "campaign-service",
                "GET",
                "/api/v1/campaigns",
                200,
                150L,
                "trace-123",
                Map.of("X-Correlation-Id", "correlation-123"),
                occurredAt
        );

        // When
        slowRequestLogConsumer.consume(event);

        // Then / Verify
        verify(slowRequestLogRepository).save(argThat(document ->
                document != null
                        && document.getId() != null
                        && !document.getId().isBlank()
                        && document.getServiceName().equals("campaign-service")
                        && document.getMethod().equals("GET")
                        && document.getPath().equals("/api/v1/campaigns")
                        && document.getStatus() == 200
                        && document.getDurationMs() == 150L
                        && document.getTraceId().equals("trace-123")
                        && document.getHeaders().get("X-Correlation-Id").equals("correlation-123")
                        && document.getOccurredAt().equals(occurredAt)
        ));
    }

    @Test
    void shouldCreateSlowRequestDocumentWithGeneratedId() {
        // Given
        Instant occurredAt = Instant.parse("2026-07-26T12:15:00Z");

        // When
        SlowRequestDocument document = new SlowRequestDocument(
                "api-gateway",
                "POST",
                "/api/v1/auth/login",
                200,
                90L,
                "trace-456",
                Map.of("Authorization", "Bearer token"),
                occurredAt
        );

        // Then
        assertThat(document.getId()).isNotNull().isNotBlank();
        assertThat(document.getServiceName()).isEqualTo("api-gateway");
        assertThat(document.getMethod()).isEqualTo("POST");
        assertThat(document.getPath()).isEqualTo("/api/v1/auth/login");
        assertThat(document.getStatus()).isEqualTo(200);
        assertThat(document.getDurationMs()).isEqualTo(90L);
        assertThat(document.getTraceId()).isEqualTo("trace-456");
        assertThat(document.getHeaders()).containsEntry("Authorization", "Bearer token");
        assertThat(document.getOccurredAt()).isEqualTo(occurredAt);
    }
}
