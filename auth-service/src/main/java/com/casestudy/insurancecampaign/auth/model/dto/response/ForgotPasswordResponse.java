package com.casestudy.insurancecampaign.auth.model.dto.response;

import java.time.Instant;

/**
 * Response payload returned after generating a password reset token.
 *
 * @param message informational response message
 * @param resetToken generated raw reset token
 * @param expiresAt expiration timestamp of the reset token
 */
public record ForgotPasswordResponse(
        String message,
        String resetToken,
        Instant expiresAt
) { }
