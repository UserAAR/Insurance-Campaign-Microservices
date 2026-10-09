package com.casestudy.insurancecampaign.auth.model.domain;

import com.casestudy.insurancecampaign.auth.model.enums.UserStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Domain model representing an application user without persistence annotations.
 *
 * @param id unique user identifier
 * @param username normalized username
 * @param email normalized email address
 * @param fullName user's full display name
 * @param roles assigned user roles
 * @param status current lifecycle status
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 */
public record AppUser(
        UUID id,
        String username,
        String email,
        String fullName,
        List<String> roles,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
