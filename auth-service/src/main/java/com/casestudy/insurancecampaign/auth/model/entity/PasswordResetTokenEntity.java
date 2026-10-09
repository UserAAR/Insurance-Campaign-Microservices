package com.casestudy.insurancecampaign.auth.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a password reset token.
 *
 * <p>
 * Raw password reset tokens are never stored directly. Instead, only the
 * SHA-256 hash of the token is persisted.
 * </p>
 */
@Getter
@Entity
@Table(name = "password_reset_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetTokenEntity {

    /**
     * Unique identifier of the password reset token.
     */
    @Id
    private UUID id;

    /**
     * User who owns this password reset token.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUserEntity user;

    /**
     * SHA-256 hash of the raw reset token.
     */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    /**
     * Expiration timestamp of the reset token.
     */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /**
     * Timestamp when the reset token was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp when the reset token was used.
     */
    @Column(name = "used_at")
    private Instant usedAt;

    /**
     * Creates a new password reset token entity.
     *
     * @param id unique token identifier
     * @param user token owner
     * @param tokenHash SHA-256 hash of the raw reset token
     * @param expiresAt expiration timestamp
     */
    public PasswordResetTokenEntity(UUID id, AppUserEntity user, String tokenHash, Instant expiresAt) {
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
     * Checks whether the reset token is expired.
     *
     * @return {@code true} if the token expiration time is before current time
     */
    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    /**
     * Checks whether the reset token has already been used.
     *
     * @return {@code true} if the token was already used
     */
    public boolean isUsed() {
        return usedAt != null;
    }

    /**
     * Marks the reset token as used.
     */
    public void markUsed() {
        usedAt = Instant.now();
    }

    /**
     * Updates the token hash value.
     *
     * <p>
     * This setter can be useful in tests or controlled persistence scenarios.
     * </p>
     *
     * @param tokenHash SHA-256 hash of the raw reset token
     */
    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

}