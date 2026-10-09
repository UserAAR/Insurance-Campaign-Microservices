package com.casestudy.insurancecampaign.auth.controller;

import com.casestudy.insurancecampaign.auth.base.AbstractRestControllerTest;
import com.casestudy.insurancecampaign.auth.model.dto.request.*;
import com.casestudy.insurancecampaign.auth.model.dto.response.ForgotPasswordResponse;
import com.casestudy.insurancecampaign.auth.model.dto.response.LogoutResponse;
import com.casestudy.insurancecampaign.auth.model.dto.response.RegisteredUserResponse;
import com.casestudy.insurancecampaign.auth.model.dto.response.TokenResponse;
import com.casestudy.insurancecampaign.auth.model.enums.UserRole;
import com.casestudy.insurancecampaign.auth.service.AuthService;
import com.casestudy.insurancecampaign.security.JwtTokenService;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends AbstractRestControllerTest {

    private static final Instant NOW = Instant.parse("2026-06-30T00:00:00Z");

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    protected JwtTokenService jwtTokenService;

    @AfterEach
    void tearDown() {
        Mockito.verifyNoInteractions(jwtTokenService);
    }

    @Test
    void shouldRegisterUserAndCreateAccessToken() throws Exception {

        // Given
        RegisterRequest registerRequest = new RegisterRequest(
                "caseuser",
                "caseuser@example.com",
                "password123",
                "Case User",
                UserRole.ROLE_USER
        );

        RegisteredUserResponse registeredUserResponse = new RegisteredUserResponse(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "caseuser",
                "caseuser@example.com",
                "Case User",
                List.of("ROLE_USER"),
                "ACTIVE",
                NOW
        );

        LoginRequest loginRequest = new LoginRequest("caseuser", "password123");

        TokenResponse tokenResponse = new TokenResponse(
                "caseuser-access-token",
                "caseuser-refresh-token",
                "Bearer",
                NOW,
                NOW.plusSeconds(120 * 60),
                NOW.plusSeconds(7 * 24 * 60 * 60),
                List.of("ROLE_USER")
        );

        // When
        Mockito.when(authService.register(registerRequest)).thenReturn(registeredUserResponse);
        Mockito.when(authService.login(loginRequest)).thenReturn(tokenResponse);

        // Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("caseuser"))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_USER"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("caseuser-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("caseuser-refresh-token"));

        // Verify
        Mockito.verify(authService).register(registerRequest);
        Mockito.verify(authService).login(loginRequest);
        Mockito.verifyNoMoreInteractions(authService);

    }

    @Test
    void shouldLoginRefreshAndLogoutDemoUser() throws Exception {

        // Given
        LoginRequest loginRequest = new LoginRequest("demo", "password");

        TokenResponse loginResponse = new TokenResponse(
                "demo-access-token",
                "demo-refresh-token",
                "Bearer",
                NOW,
                NOW.plusSeconds(120 * 60),
                NOW.plusSeconds(7 * 24 * 60 * 60),
                List.of("ROLE_USER", "ROLE_ADMIN")
        );

        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest("demo-refresh-token");

        TokenResponse refreshTokenResponse = new TokenResponse(
                "demo-rotated-access-token",
                "demo-rotated-refresh-token",
                "Bearer",
                NOW,
                NOW.plusSeconds(120 * 60),
                NOW.plusSeconds(7 * 24 * 60 * 60),
                List.of("ROLE_USER", "ROLE_ADMIN")
        );

        LogoutRequest logoutRequest = new LogoutRequest("demo-rotated-refresh-token");

        LogoutResponse logoutResponse = new LogoutResponse("Logout completed successfully");

        // When
        Mockito.when(authService.login(loginRequest)).thenReturn(loginResponse);
        Mockito.when(authService.refreshToken(refreshTokenRequest)).thenReturn(refreshTokenResponse);
        Mockito.when(authService.logout(logoutRequest)).thenReturn(logoutResponse);

        // Then
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("demo-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("demo-refresh-token"))
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String refreshToken = loginJson.path("data").path("refreshToken").asText();

        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("demo-rotated-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("demo-rotated-refresh-token"))
                .andReturn();

        JsonNode refreshJson = objectMapper.readTree(refreshResult.getResponse().getContentAsString());
        String rotatedRefreshToken = refreshJson.path("data").path("refreshToken").asText();

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LogoutRequest(rotatedRefreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Logout completed successfully"));

        // Verify
        Mockito.verify(authService).login(loginRequest);
        Mockito.verify(authService).refreshToken(refreshTokenRequest);
        Mockito.verify(authService).logout(logoutRequest);
        Mockito.verifyNoMoreInteractions(authService);

    }

    @Test
    void shouldLoginDefaultRoleUser() throws Exception {

        // Given
        LoginRequest loginRequest = new LoginRequest("user", "password");

        TokenResponse tokenResponse = new TokenResponse(
                "user-access-token",
                "user-refresh-token",
                "Bearer",
                NOW,
                NOW.plusSeconds(120 * 60),
                NOW.plusSeconds(7 * 24 * 60 * 60),
                List.of("ROLE_USER")
        );

        // When
        Mockito.when(authService.login(loginRequest)).thenReturn(tokenResponse);

        // Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("user-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("user-refresh-token"))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_USER"));

        // Verify
        Mockito.verify(authService).login(loginRequest);
        Mockito.verifyNoMoreInteractions(authService);

    }

    @Test
    void shouldGenerateForgotPasswordTokenAndResetPassword() throws Exception {

        // Given
        ForgotPasswordRequest forgotPasswordRequest = new ForgotPasswordRequest("admin@example.com");

        ForgotPasswordResponse forgotPasswordResponse = new ForgotPasswordResponse(
                "Password reset token was generated. In a production system this token should be sent by email.",
                "admin-reset-token",
                NOW.plusSeconds(15 * 60)
        );

        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest(
                "admin-reset-token",
                "newPassword123"
        );

        LogoutResponse resetPasswordResponse = new LogoutResponse(
                "Password was reset successfully. Active refresh tokens were revoked."
        );

        LoginRequest loginRequest = new LoginRequest("admin", "newPassword123");

        TokenResponse loginResponse = new TokenResponse(
                "admin-new-access-token",
                "admin-new-refresh-token",
                "Bearer",
                NOW,
                NOW.plusSeconds(120 * 60),
                NOW.plusSeconds(7 * 24 * 60 * 60),
                List.of("ROLE_ADMIN")
        );

        // When
        Mockito.when(authService.forgotPassword(forgotPasswordRequest)).thenReturn(forgotPasswordResponse);
        Mockito.when(authService.resetPassword(resetPasswordRequest)).thenReturn(resetPasswordResponse);
        Mockito.when(authService.login(loginRequest)).thenReturn(loginResponse);

        // Then
        MvcResult forgotPasswordResult = mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotPasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resetToken").value("admin-reset-token"))
                .andReturn();

        JsonNode forgotPasswordJson = objectMapper.readTree(forgotPasswordResult.getResponse().getContentAsString());
        String resetToken = forgotPasswordJson.path("data").path("resetToken").asText();

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ResetPasswordRequest(resetToken, "newPassword123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Password was reset successfully. Active refresh tokens were revoked."));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("admin-new-access-token"));

        // Verify
        Mockito.verify(authService).forgotPassword(forgotPasswordRequest);
        Mockito.verify(authService).resetPassword(resetPasswordRequest);
        Mockito.verify(authService).login(loginRequest);
        Mockito.verifyNoMoreInteractions(authService);

    }

}