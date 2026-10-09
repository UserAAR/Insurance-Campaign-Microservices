package com.casestudy.insurancecampaign.auth.model.entity;

import com.casestudy.insurancecampaign.auth.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing an application user inside the auth-service.
 *
 * <p>
 * This entity stores user identity information, encoded password,
 * assigned roles, account lifecycle status, and audit timestamps.
 * </p>
 */
@Getter
@Entity
@Table(name = "app_users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppUserEntity {

    /**
     * Unique identifier of the user.
     */
    @Id
    private UUID id;

    /**
     * Unique username used for authentication.
     */
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    /**
     * Unique email address of the user.
     */
    @Column(unique = true, length = 150)
    private String email;

    /**
     * Full display name of the user.
     */
    @Column(name = "full_name", length = 150)
    private String fullName;

    /**
     * Encoded user password.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Comma-separated role values assigned to the user.
     *
     * <p>
     * Example: {@code ROLE_USER,ROLE_ADMIN}
     * </p>
     */
    @Column(nullable = false)
    private String roles;

    /**
     * Current lifecycle status of the user.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * Timestamp when the user was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp when the user was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Creates a new active application user.
     *
     * @param id unique user identifier
     * @param username username used for login
     * @param email email address of the user
     * @param fullName full display name of the user
     * @param password encoded password
     * @param roles comma-separated role values
     */
    public AppUserEntity(UUID id, String username, String email, String fullName, String password, String roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.roles = roles;
        this.status = UserStatus.ACTIVE;
    }

    /**
     * Initializes creation and update timestamps before persisting the entity.
     */
    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = UserStatus.ACTIVE;
        }
    }

    /**
     * Updates the modification timestamp before updating the entity.
     */
    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Returns user roles as a normalized list.
     *
     * @return list of user roles
     */
    public List<String> getRoles() {
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .toList();
    }

    /**
     * Checks whether the user account is active.
     *
     * @return {@code true} if the user status is {@link UserStatus#ACTIVE}
     */
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(status);
    }

    /**
     * Changes the current encoded password.
     *
     * @param encodedPassword newly encoded password
     */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

}