package com.casestudy.insurancecampaign.log.service;

import com.casestudy.insurancecampaign.log.base.AbstractBaseServiceTest;
import com.casestudy.insurancecampaign.log.model.document.AuthAuditLogDocument;
import com.casestudy.insurancecampaign.log.model.dto.request.AuthAuditLogRequest;
import com.casestudy.insurancecampaign.log.repository.AuthAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

class AuthAuditLogServiceTest extends AbstractBaseServiceTest {

    @Mock
    private AuthAuditLogRepository authAuditLogRepository;

    private AuthAuditLogService authAuditLogService;

    @BeforeEach
    void setUp() {
        authAuditLogService = new AuthAuditLogService(authAuditLogRepository);
    }

    @Test
    void shouldSaveAuthAuditLogDocument() {
        // Given
        Instant occurredAt = Instant.parse("2026-07-26T12:00:00Z");

        AuthAuditLogRequest request = new AuthAuditLogRequest(
                "auth-service",
                "LOGIN_SUCCESS",
                "demo",
                "User logged in successfully",
                "SUCCESS",
                occurredAt
        );

        // When
        authAuditLogService.save(request);

        // Then & Verify
        verify(authAuditLogRepository).save(argThat(document ->
                document != null
                        && document.getId() != null
                        && !document.getId().isBlank()
                        && document.getServiceName().equals("auth-service")
                        && document.getEventType().equals("LOGIN_SUCCESS")
                        && document.getUsernameOrEmail().equals("demo")
                        && document.getMessage().equals("User logged in successfully")
                        && document.getStatus().equals("SUCCESS")
                        && document.getOccurredAt().equals(occurredAt)
        ));
    }

    @Test
    void shouldCreateAuthAuditLogDocumentWithGeneratedId() {
        // Given
        Instant occurredAt = Instant.parse("2026-07-26T12:00:00Z");

        // When
        AuthAuditLogDocument document = new AuthAuditLogDocument(
                "auth-service",
                "LOGIN_FAILURE",
                "missing-user",
                "FAILED",
                "Invalid username/email or password",
                occurredAt
        );

        // Then
        assertThat(document.getId()).isNotNull().isNotBlank();
        assertThat(document.getServiceName()).isEqualTo("auth-service");
        assertThat(document.getEventType()).isEqualTo("LOGIN_FAILURE");
        assertThat(document.getUsernameOrEmail()).isEqualTo("missing-user");
        assertThat(document.getStatus()).isEqualTo("FAILED");
        assertThat(document.getMessage()).isEqualTo("Invalid username/email or password");
        assertThat(document.getOccurredAt()).isEqualTo(occurredAt);
    }

}