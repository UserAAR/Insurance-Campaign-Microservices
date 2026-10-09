package com.casestudy.insurancecampaign.auth.service;

import com.casestudy.insurancecampaign.auth.base.AbstractBaseServiceTest;
import com.casestudy.insurancecampaign.auth.client.LogServiceHttpClient;
import com.casestudy.insurancecampaign.auth.model.domain.AuthAuditLog;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(OutputCaptureExtension.class)
class AuthAuditServiceTest extends AbstractBaseServiceTest {

    private AuthAuditService authAuditService;

    @Mock
    private LogServiceHttpClient logServiceHttpClient;

    @BeforeEach
    void setUp() {
        authAuditService = new AuthAuditService(logServiceHttpClient);
    }

    @Test
    void shouldPublishRegisterAuditAsync() {
        // Given
        String username = "demo";
        String email = "demo@example.com";
        String role = "ROLE_USER";

        // When
        authAuditService.publishRegisterAuditAsync(username, email, role);

        // Then
        verify(logServiceHttpClient).sendAuthAuditLog(argThat(auditLog ->
                auditLog.serviceName().equals("auth-service")
                        && auditLog.eventType().equals("REGISTER")
                        && auditLog.usernameOrEmail().equals("demo")
                        && auditLog.message().equals("User registered successfully. email=demo@example.com, role=ROLE_USER")
                        && auditLog.status().equals("SUCCESS")
                        && auditLog.occurredAt() != null
        ));
    }

    @Test
    void shouldPublishLoginAuditAsync() {
        // Given
        String usernameOrEmail = "demo";

        // When
        authAuditService.publishLoginAuditAsync(usernameOrEmail);

        // Then
        verify(logServiceHttpClient).sendAuthAuditLog(argThat(auditLog ->
                auditLog.serviceName().equals("auth-service")
                        && auditLog.eventType().equals("LOGIN_SUCCESS")
                        && auditLog.usernameOrEmail().equals("demo")
                        && auditLog.message().equals("Login completed successfully")
                        && auditLog.status().equals("SUCCESS")
                        && auditLog.occurredAt() != null
        ));
    }

    @Test
    void shouldPublishLoginSuccessAuditAsync() {
        // Given
        String usernameOrEmail = "demo@example.com";

        // When & Then
        authAuditService.publishLoginSuccessAuditAsync(usernameOrEmail);

        // Verify
        verify(logServiceHttpClient).sendAuthAuditLog(argThat(auditLog ->
                auditLog.serviceName().equals("auth-service")
                        && auditLog.eventType().equals("LOGIN_SUCCESS")
                        && auditLog.usernameOrEmail().equals("demo@example.com")
                        && auditLog.message().equals("Login completed successfully")
                        && auditLog.status().equals("SUCCESS")
                        && auditLog.occurredAt() != null
        ));

    }

    @Test
    void shouldPublishLoginFailureAuditAsync() {
        // Given
        String usernameOrEmail = "demo@example.com";
        String reason = "Invalid username/email or password";

        // When
        authAuditService.publishLoginFailureAuditAsync(usernameOrEmail, reason);

        // Then
        verify(logServiceHttpClient).sendAuthAuditLog(argThat(auditLog ->
                auditLog.serviceName().equals("auth-service")
                        && auditLog.eventType().equals("LOGIN_FAILURE")
                        && auditLog.usernameOrEmail().equals("demo@example.com")
                        && auditLog.message().equals("Invalid username/email or password")
                        && auditLog.status().equals("FAILED")
                        && auditLog.occurredAt() != null
        ));
    }

    @Test
    void shouldPublishRefreshTokenAuditAsync() {
        // Given
        String username = "demo";

        // When
        authAuditService.publishRefreshTokenAuditAsync(username);

        // Then
        verify(logServiceHttpClient).sendAuthAuditLog(argThat(auditLog ->
                auditLog.serviceName().equals("auth-service")
                        && auditLog.eventType().equals("REFRESH_TOKEN")
                        && auditLog.usernameOrEmail().equals("demo")
                        && auditLog.message().equals("Refresh token rotated successfully")
                        && auditLog.status().equals("SUCCESS")
                        && auditLog.occurredAt() != null
        ));
    }

    @Test
    void shouldPublishForgotPasswordAuditAsync() {
        // Given
        String usernameOrEmail = "demo@example.com";

        // When
        authAuditService.publishForgotPasswordAuditAsync(usernameOrEmail);

        // Then
        verify(logServiceHttpClient).sendAuthAuditLog(argThat(auditLog ->
                auditLog.serviceName().equals("auth-service")
                        && auditLog.eventType().equals("FORGOT_PASSWORD")
                        && auditLog.usernameOrEmail().equals("demo@example.com")
                        && auditLog.message().equals("Password reset token generated")
                        && auditLog.status().equals("SUCCESS")
                        && auditLog.occurredAt() != null
        ));
    }

    @Test
    void shouldPublishResetPasswordAuditAsync() {
        // Given
        String username = "demo";

        // When
        authAuditService.publishResetPasswordAuditAsync(username);

        // Then
        verify(logServiceHttpClient).sendAuthAuditLog(argThat(auditLog ->
                auditLog.serviceName().equals("auth-service")
                        && auditLog.eventType().equals("RESET_PASSWORD")
                        && auditLog.usernameOrEmail().equals("demo")
                        && auditLog.message().equals("Password reset successfully")
                        && auditLog.status().equals("SUCCESS")
                        && auditLog.occurredAt() != null
        ));
    }

    @Test
    void shouldPublishLogoutAuditAsync() {
        // Given
        String username = "demo";

        // When
        authAuditService.publishLogoutAuditAsync(username);

        // Then
        verify(logServiceHttpClient).sendAuthAuditLog(argThat(auditLog ->
                auditLog.serviceName().equals("auth-service")
                        && auditLog.eventType().equals("LOGOUT")
                        && auditLog.usernameOrEmail().equals("demo")
                        && auditLog.message().equals("Logout completed successfully")
                        && auditLog.status().equals("SUCCESS")
                        && auditLog.occurredAt() != null
        ));
    }

    @Test
    void shouldWriteInfoLogWhenAuditIsSentSuccessfully(CapturedOutput output) {
        // Given
        String usernameOrEmail = "demo@example.com";

        // When
        authAuditService.publishLoginSuccessAuditAsync(usernameOrEmail);

        // Then
        assertThat(output)
                .contains("Auth audit sent to log-service")
                .contains("eventType=LOGIN_SUCCESS")
                .contains("usernameOrEmail=demo@example.com")
                .contains("status=SUCCESS");
    }

    @Test
    void shouldNotThrowExceptionWhenLogServiceClientFails(CapturedOutput output) {
        // Given
        String usernameOrEmail = "demo@example.com";

        Mockito.doThrow(new RuntimeException("Connection refused"))
                .when(logServiceHttpClient)
                .sendAuthAuditLog(any(AuthAuditLog.class));

        // When
        authAuditService.publishLoginSuccessAuditAsync(usernameOrEmail);

        // Then
        assertThat(output)
                .contains("Auth audit could not be sent to log-service")
                .contains("eventType=LOGIN_SUCCESS")
                .contains("usernameOrEmail=demo@example.com")
                .contains("status=SUCCESS")
                .contains("reason=Connection refused");
    }

    @Test
    void shouldNotThrowExceptionWhenCircuitBreakerIsOpen(CapturedOutput output) {

        // Given
        String usernameOrEmail = "demo@example.com";

        CallNotPermittedException exception =
                CallNotPermittedException.createCallNotPermittedException(
                        CircuitBreaker.ofDefaults("logServiceHttpClient")
                );

        // When
        Mockito.doThrow(exception)
                .when(logServiceHttpClient)
                .sendAuthAuditLog(any(AuthAuditLog.class));

        // Then
        authAuditService.publishLoginSuccessAuditAsync(usernameOrEmail);

        assertThat(output)
                .contains("Auth audit skipped because log-service circuit breaker is open")
                .contains("eventType=LOGIN_SUCCESS")
                .contains("usernameOrEmail=demo@example.com")
                .contains("status=SUCCESS");

        // Verify
        verify(logServiceHttpClient).sendAuthAuditLog(argThat(auditLog ->
                auditLog.serviceName().equals("auth-service")
                        && auditLog.eventType().equals("LOGIN_SUCCESS")
                        && auditLog.usernameOrEmail().equals("demo@example.com")
                        && auditLog.message().equals("Login completed successfully")
                        && auditLog.status().equals("SUCCESS")
                        && auditLog.occurredAt() != null
        ));

    }

    @Test
    void shouldNotThrowExceptionWhenBulkheadIsFull(CapturedOutput output) {

        // Given
        String usernameOrEmail = "demo@example.com";

        BulkheadFullException exception =
                BulkheadFullException.createBulkheadFullException(
                        Bulkhead.ofDefaults("logServiceHttpClient")
                );

        // When
        Mockito.doThrow(exception)
                .when(logServiceHttpClient)
                .sendAuthAuditLog(any(AuthAuditLog.class));

        // Then
        authAuditService.publishLoginSuccessAuditAsync(usernameOrEmail);

        assertThat(output)
                .contains("Auth audit skipped because log-service bulkhead is full")
                .contains("eventType=LOGIN_SUCCESS")
                .contains("usernameOrEmail=demo@example.com")
                .contains("status=SUCCESS");

        // Verify
        verify(logServiceHttpClient).sendAuthAuditLog(argThat(auditLog ->
                auditLog.serviceName().equals("auth-service")
                        && auditLog.eventType().equals("LOGIN_SUCCESS")
                        && auditLog.usernameOrEmail().equals("demo@example.com")
                        && auditLog.message().equals("Login completed successfully")
                        && auditLog.status().equals("SUCCESS")
                        && auditLog.occurredAt() != null
        ));

    }

}