package com.casestudy.insurancecampaign.auth.controller;

import com.casestudy.insurancecampaign.auth.model.dto.request.*;
import com.casestudy.insurancecampaign.auth.model.dto.response.ForgotPasswordResponse;
import com.casestudy.insurancecampaign.auth.model.dto.response.LogoutResponse;
import com.casestudy.insurancecampaign.auth.model.dto.response.RegisteredUserResponse;
import com.casestudy.insurancecampaign.auth.model.dto.response.TokenResponse;
import com.casestudy.insurancecampaign.auth.service.AuthService;
import com.casestudy.insurancecampaign.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller responsible for authentication operations.
 *
 * <p>
 * This controller exposes endpoints for user registration, login, refresh-token
 * rotation, password reset flow, and logout operations.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Creates a new {@link AuthController}.
     *
     * @param authService service that handles authentication business logic
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new ROLE_USER or ROLE_ADMIN account.
     *
     * @param request registration request containing username, email, password,
     *                full name, and role
     * @return registered user information wrapped with {@link ApiResponse}
     */
    @Operation(summary = "Register ROLE_USER or ROLE_ADMIN account inside auth-service")
    @PostMapping("/register")
    public ApiResponse<RegisteredUserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    /**
     * Authenticates a user and creates JWT access and refresh tokens.
     *
     * @param request login request containing username/email and password
     * @return token response containing access token, refresh token, expiration
     * information, and user roles
     */
    @Operation(summary = "Create JWT access token and refresh token")
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    /**
     * Creates a new access token by rotating an existing refresh token.
     *
     * @param request refresh-token request containing the current refresh token
     * @return new token response with rotated refresh token
     */
    @Operation(summary = "Create a new access token by rotating refresh token")
    @PostMapping("/refresh-token")
    public ApiResponse<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refreshToken(request));
    }

    /**
     * Generates a password reset token for an existing active user.
     *
     * @param request forgot-password request containing username or email
     * @return generated password reset token information
     */
    @Operation(summary = "Generate password reset token for ROLE_USER or ROLE_ADMIN")
    @PostMapping("/forgot-password")
    public ApiResponse<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ApiResponse.success(authService.forgotPassword(request));
    }

    /**
     * Resets a user's password by using a valid password reset token.
     *
     * <p>
     * After a successful password reset, active refresh tokens are revoked.
     * </p>
     *
     * @param request reset-password request containing reset token and new password
     * @return logout-style response message describing the reset result
     */
    @Operation(summary = "Reset password by using generated forgot-password token")
    @PostMapping("/reset-password")
    public ApiResponse<LogoutResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ApiResponse.success(authService.resetPassword(request));
    }

    /**
     * Logs out a user by revoking the provided refresh token.
     *
     * @param request logout request containing refresh token
     * @return logout response message
     */
    @Operation(summary = "Logout by revoking refresh token")
    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout(@Valid @RequestBody LogoutRequest request) {
        return ApiResponse.success(authService.logout(request));
    }

}
