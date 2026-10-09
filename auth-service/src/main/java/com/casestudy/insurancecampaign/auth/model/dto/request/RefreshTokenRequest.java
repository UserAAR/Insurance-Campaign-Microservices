package com.casestudy.insurancecampaign.auth.model.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload used to rotate a refresh token.
 *
 * @param refreshToken current refresh token
 */
public record RefreshTokenRequest(
        @NotBlank String refreshToken
) { }
