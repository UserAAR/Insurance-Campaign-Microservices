package com.casestudy.insurancecampaign.auth.model.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model representing a password reset token without persistence annotations.
 *
 * @param id unique reset token identifier
 * @param user token owner
 * @param tokenHash SHA-256 hash of the raw reset token
 * @param expiresAt token expiration timestamp
 * @param createdAt token creation timestamp
 * @param usedAt token usage timestamp
 */
public record PasswordResetToken(
        UUID id,
        AppUser user,
        String tokenHash,
        Instant expiresAt,
        Instant createdAt,
        Instant usedAt
) {
}
