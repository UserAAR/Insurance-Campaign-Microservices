package com.casestudy.insurancecampaign.auth.model.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload used for logout operation.
 *
 * @param refreshToken refresh token that should be revoked
 */
public record LogoutRequest(
        @NotBlank String refreshToken
) { }
