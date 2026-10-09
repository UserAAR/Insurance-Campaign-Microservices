package com.casestudy.insurancecampaign.auth.model.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload used for user login.
 *
 * @param usernameOrEmail username or email address of the user
 * @param password raw password entered by the user
 */
public record LoginRequest(
        @NotBlank String usernameOrEmail,
        @NotBlank String password
) { }
