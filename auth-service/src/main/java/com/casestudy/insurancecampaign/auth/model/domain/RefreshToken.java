package com.casestudy.insurancecampaign.auth.model.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model representing a refresh token without persistence annotations.
 *
 * @param id unique refresh token identifier
 * @param user token owner
 * @param tokenHash SHA-256 hash of the raw refresh token
 * @param expiresAt token expiration timestamp
 * @param createdAt token creation timestamp
 * @param revokedAt token revocation timestamp
 */
public record RefreshToken(
        UUID id,
        AppUser user,
        String tokenHash,
        Instant expiresAt,
        Instant createdAt,
        Instant revokedAt
) {
}
