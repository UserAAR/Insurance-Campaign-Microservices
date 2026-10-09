package com.casestudy.insurancecampaign.auth.service;

import com.casestudy.insurancecampaign.auth.client.LogServiceHttpClient;
import com.casestudy.insurancecampaign.auth.model.domain.AuthAuditLog;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service responsible for publishing authentication audit events.
 *
 * <p>
 * Audit events are sent to log-service through {@link LogServiceHttpClient}.
 * Circuit breaker, retry and bulkhead are defined directly on the HTTP exchange
 * client method. Failures are logged locally and do not interrupt the
 * authentication flow.
 * </p>
 */
@Service
public class AuthAuditService {

    private static final Logger log = LoggerFactory.getLogger(AuthAuditService.class);

    private static final String SERVICE_NAME = "auth-service";
    private static final String SUCCESS = "SUCCESS";
    private static final String FAILED = "FAILED";

    private final LogServiceHttpClient logServiceHttpClient;

    /**
     * Creates a new {@link AuthAuditService}.
     *
     * @param logServiceHttpClient HTTP exchange client for log-service
     */
    public AuthAuditService(LogServiceHttpClient logServiceHttpClient) {
        this.logServiceHttpClient = logServiceHttpClient;
    }

    /**
     * Publishes a successful user registration audit event asynchronously.
     *
     * @param username registered username
     * @param email registered email
     * @param role assigned user role
     */
    @Async
    public void publishRegisterAuditAsync(String username, String email, String role) {
        publishAudit(
                "REGISTER",
                username,
                "User registered successfully. email=" + email + ", role=" + role,
                SUCCESS
        );
    }

    /**
     * Publishes a generic login audit event asynchronously.
     *
     * <p>
     * This method is kept for backward compatibility and delegates to the login
     * success audit event.
     * </p>
     *
     * @param usernameOrEmail username or email used during login
     */
    @Async
    public void publishLoginAuditAsync(String usernameOrEmail) {
        publishAudit("LOGIN_SUCCESS", usernameOrEmail, "Login completed successfully", SUCCESS);
    }

    /**
     * Publishes a successful login audit event asynchronously.
     *
     * @param usernameOrEmail username or email used during login
     */
    @Async
    public void publishLoginSuccessAuditAsync(String usernameOrEmail) {
        publishAudit("LOGIN_SUCCESS", usernameOrEmail, "Login completed successfully", SUCCESS);
    }

    /**
     * Publishes a failed login audit event asynchronously.
     *
     * @param usernameOrEmail username or email used during login
     * @param reason failure reason
     */
    @Async
    public void publishLoginFailureAuditAsync(String usernameOrEmail, String reason) {
        publishAudit("LOGIN_FAILURE", usernameOrEmail, reason, FAILED);
    }

    /**
     * Publishes a refresh-token rotation audit event asynchronously.
     *
     * @param username username that owns the rotated refresh token
     */
    @Async
    public void publishRefreshTokenAuditAsync(String username) {
        publishAudit("REFRESH_TOKEN", username, "Refresh token rotated successfully", SUCCESS);
    }

    /**
     * Publishes a forgot-password audit event asynchronously.
     *
     * @param usernameOrEmail username or email used during forgot-password request
     */
    @Async
    public void publishForgotPasswordAuditAsync(String usernameOrEmail) {
        publishAudit("FORGOT_PASSWORD", usernameOrEmail, "Password reset token generated", SUCCESS);
    }

    /**
     * Publishes a reset-password audit event asynchronously.
     *
     * @param username username whose password was reset
     */
    @Async
    public void publishResetPasswordAuditAsync(String username) {
        publishAudit("RESET_PASSWORD", username, "Password reset successfully", SUCCESS);
    }

    /**
     * Publishes a logout audit event asynchronously.
     *
     * @param username username that logged out
     */
    @Async
    public void publishLogoutAuditAsync(String username) {
        publishAudit("LOGOUT", username, "Logout completed successfully", SUCCESS);
    }

    private void publishAudit(String eventType, String usernameOrEmail, String message, String status) {
        AuthAuditLog auditLog = new AuthAuditLog(
                SERVICE_NAME,
                eventType,
                usernameOrEmail,
                message,
                status,
                Instant.now()
        );

        try {
            logServiceHttpClient.sendAuthAuditLog(auditLog);
            log.info(
                    "Auth audit sent to log-service. eventType={}, usernameOrEmail={}, status={}",
                    eventType,
                    usernameOrEmail,
                    status
            );
        } catch (CallNotPermittedException exception) {
            log.warn(
                    "Auth audit skipped because log-service circuit breaker is open. eventType={}, usernameOrEmail={}, status={}, reason={}",
                    eventType,
                    usernameOrEmail,
                    status,
                    exception.getMessage()
            );
        } catch (BulkheadFullException exception) {
            log.warn(
                    "Auth audit skipped because log-service bulkhead is full. eventType={}, usernameOrEmail={}, status={}, reason={}",
                    eventType,
                    usernameOrEmail,
                    status,
                    exception.getMessage()
            );
        } catch (Exception exception) {
            log.warn(
                    "Auth audit could not be sent to log-service. eventType={}, usernameOrEmail={}, status={}, reason={}",
                    eventType,
                    usernameOrEmail,
                    status,
                    exception.getMessage()
            );
        }
    }
}
