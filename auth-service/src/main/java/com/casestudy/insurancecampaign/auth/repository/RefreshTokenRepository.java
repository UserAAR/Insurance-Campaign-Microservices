package com.casestudy.insurancecampaign.auth.repository;

import com.casestudy.insurancecampaign.auth.model.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for accessing refresh token records.
 *
 * <p>
 * Refresh tokens are stored as SHA-256 hashes instead of raw token values.
 * </p>
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    /**
     * Finds a refresh token by its hashed token value.
     *
     * @param tokenHash SHA-256 hash of the raw refresh token
     * @return optional refresh token entity
     */
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    /**
     * Revokes all active refresh tokens for the given user.
     *
     * <p>
     * Only tokens whose {@code revokedAt} value is {@code null} are updated.
     * </p>
     *
     * @param userId user identifier
     * @param revokedAt revocation timestamp
     */
    @Modifying
    @Query("update RefreshTokenEntity token set token.revokedAt = :revokedAt where token.user.id = :userId and token.revokedAt is null")
    void revokeAllActiveTokensForUser(@Param("userId") UUID userId, @Param("revokedAt") Instant revokedAt);

}
