package com.casestudy.insurancecampaign.auth.model.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Response payload containing authentication token information.
 *
 * @param accessToken JWT access token
 * @param refreshToken refresh token used to obtain a new access token
 * @param tokenType token type, usually {@code Bearer}
 * @param issuedAt token creation timestamp
 * @param accessTokenExpiresAt access token expiration timestamp
 * @param refreshTokenExpiresAt refresh token expiration timestamp
 * @param roles roles assigned to the authenticated user
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Instant issuedAt,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt,
        List<String> roles
) { }
