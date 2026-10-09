package com.casestudy.insurancecampaign.auth.repository;

import com.casestudy.insurancecampaign.auth.model.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for accessing password reset token records.
 *
 * <p>
 * Password reset tokens are stored as SHA-256 hashes instead of raw token values.
 * </p>
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    /**
     * Finds a password reset token by its hashed token value.
     *
     * @param tokenHash SHA-256 hash of the raw reset token
     * @return optional password reset token entity
     */
    Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);

}
