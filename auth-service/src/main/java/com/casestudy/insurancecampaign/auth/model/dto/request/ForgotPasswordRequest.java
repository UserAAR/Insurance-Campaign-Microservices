package com.casestudy.insurancecampaign.auth.model.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload used to start the forgot-password flow.
 *
 * @param usernameOrEmail username or email address of the user requesting password reset
 */

public record ForgotPasswordRequest(
        @NotBlank String usernameOrEmail
) { }
