package com.casestudy.insurancecampaign.auth.model.dto.request;

import com.casestudy.insurancecampaign.auth.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload used to register a new user account.
 *
 * @param username unique username
 * @param email unique email address
 * @param password raw password that will be encoded before persistence
 * @param fullName full display name of the user
 * @param role role assigned to the user
 */
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 100) String username,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Size(min = 2, max = 150) String fullName,
        @NotNull UserRole role
) { }
