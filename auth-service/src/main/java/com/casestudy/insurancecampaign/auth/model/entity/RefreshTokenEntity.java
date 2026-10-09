package com.casestudy.insurancecampaign.auth.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a refresh token.
 *
 * <p>
 * Raw refresh tokens are never stored directly. Only SHA-256 hashed token
 * values are persisted for security reasons.
 * </p>
 */
@Getter
@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshTokenEntity {

    /**
     * Unique identifier of the refresh token.
     */
    @Id
    private UUID id;

    /**
     * User who owns this refresh token.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUserEntity user;

    /**
     * SHA-256 hash of the raw refresh token.
     */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    /**
     * Expiration timestamp of the refresh token.
     */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /**
     * Timestamp when the refresh token was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp when the refresh token was revoked.
     */
    @Column(name = "revoked_at")
    private Instant revokedAt;

    /**
     * Creates a new refresh token entity.
     *
     * @param id unique token identifier
     * @param user token owner
     * @param tokenHash SHA-256 hash of the raw refresh token
     * @param expiresAt expiration timestamp
     */
    public RefreshTokenEntity(UUID id, AppUserEntity user, String tokenHash, Instant expiresAt) {
        this.id = id;
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    /**
     * Initializes creation timestamp before persisting the entity.
     */
    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }

    /**
     * Checks whether the refresh token is expired.
     *
     * @return {@code true} if the token expiration time is before current time
     */
    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    /**
     * Checks whether the refresh token has already been revoked.
     *
     * @return {@code true} if the token has a revocation timestamp
     */
    public boolean isRevoked() {
        return revokedAt != null;
    }

    /**
     * Revokes the refresh token by setting the revocation timestamp.
     */
    public void revoke() {
        revokedAt = Instant.now();
    }

}