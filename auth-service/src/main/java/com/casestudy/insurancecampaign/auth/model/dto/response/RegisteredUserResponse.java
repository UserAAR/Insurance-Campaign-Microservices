package com.casestudy.insurancecampaign.auth.model.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response payload returned after successful user registration.
 *
 * @param id unique user identifier
 * @param username registered username
 * @param email registered email address
 * @param fullName full name of the registered user
 * @param roles assigned user roles
 * @param status current user status
 * @param createdAt user creation timestamp
 */
public record RegisteredUserResponse(
        UUID id,
        String username,
        String email,
        String fullName,
        List<String> roles,
        String status,
        Instant createdAt
) { }
