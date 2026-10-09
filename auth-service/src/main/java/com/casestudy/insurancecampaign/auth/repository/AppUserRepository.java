package com.casestudy.insurancecampaign.auth.repository;

import com.casestudy.insurancecampaign.auth.model.entity.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for accessing application user records.
 *
 * <p>
 * Provides lookup and existence-check operations by username and email.
 * </p>
 */
public interface AppUserRepository extends JpaRepository<AppUserEntity, UUID> {

    /**
     * Finds a user by username.
     *
     * @param username normalized username
     * @return optional user entity
     */
    Optional<AppUserEntity> findByUsername(String username);

    /**
     * Finds a user by email address.
     *
     * @param email normalized email address
     * @return optional user entity
     */
    Optional<AppUserEntity> findByEmail(String email);

    /**
     * Checks whether a user exists with the given username.
     *
     * @param username normalized username
     * @return {@code true} if username already exists, otherwise {@code false}
     */
    boolean existsByUsername(String username);

    /**
     * Checks whether a user exists with the given email address.
     *
     * @param email normalized email address
     * @return {@code true} if email already exists, otherwise {@code false}
     */
    boolean existsByEmail(String email);

}
