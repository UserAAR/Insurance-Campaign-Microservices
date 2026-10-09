package com.casestudy.insurancecampaign.auth.service;

import com.casestudy.insurancecampaign.auth.base.AbstractBaseServiceTest;
import com.casestudy.insurancecampaign.auth.model.dto.request.*;
import com.casestudy.insurancecampaign.auth.model.dto.response.ForgotPasswordResponse;
import com.casestudy.insurancecampaign.auth.model.dto.response.LogoutResponse;
import com.casestudy.insurancecampaign.auth.model.dto.response.RegisteredUserResponse;
import com.casestudy.insurancecampaign.auth.model.dto.response.TokenResponse;
import com.casestudy.insurancecampaign.auth.model.entity.AppUserEntity;
import com.casestudy.insurancecampaign.auth.model.entity.PasswordResetTokenEntity;
import com.casestudy.insurancecampaign.auth.model.entity.RefreshTokenEntity;
import com.casestudy.insurancecampaign.auth.model.enums.UserRole;
import com.casestudy.insurancecampaign.auth.model.mapper.AuthMapper;
import com.casestudy.insurancecampaign.auth.repository.AppUserRepository;
import com.casestudy.insurancecampaign.auth.repository.PasswordResetTokenRepository;
import com.casestudy.insurancecampaign.auth.repository.RefreshTokenRepository;
import com.casestudy.insurancecampaign.auth.util.TokenHashUtils;
import com.casestudy.insurancecampaign.common.exception.BusinessException;
import com.casestudy.insurancecampaign.common.exception.ResourceNotFoundException;
import com.casestudy.insurancecampaign.security.JwtProperties;
import com.casestudy.insurancecampaign.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest extends AbstractBaseServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Instant NOW = Instant.parse("2026-06-30T00:00:00Z");

    private AuthService authService;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthAuditService authAuditService;

    @Mock
    private AuthMapper authMapper;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                appUserRepository,
                refreshTokenRepository,
                passwordResetTokenRepository,
                jwtTokenService,
                jwtProperties,
                passwordEncoder,
                authAuditService,
                7,
                15,
                authMapper
        );
    }

    @Test
    void shouldRegisterUser() {
        // Given
        RegisterRequest request = new RegisterRequest(
                " CaseUser ",
                " CaseUser@Example.com ",
                "password123",
                " Case User ",
                UserRole.ROLE_USER
        );

        AppUserEntity user = new AppUserEntity(
                USER_ID,
                "caseuser",
                "caseuser@example.com",
                "Case User",
                "encoded-password",
                "ROLE_USER"
        );

        RegisteredUserResponse registeredUserResponse = new RegisteredUserResponse(
                USER_ID,
                "caseuser",
                "caseuser@example.com",
                "Case User",
                List.of("ROLE_USER"),
                "ACTIVE",
                NOW
        );

        when(appUserRepository.existsByUsername("caseuser")).thenReturn(false);
        when(appUserRepository.existsByEmail("caseuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        when(authMapper.toEntity(
                any(UUID.class),
                eq("caseuser"),
                eq("caseuser@example.com"),
                eq("Case User"),
                eq("encoded-password"),
                eq("ROLE_USER")
        )).thenReturn(user);

        when(appUserRepository.save(user)).thenReturn(user);
        when(authMapper.toRegisteredUserResponse(user)).thenReturn(registeredUserResponse);

        // When
        RegisteredUserResponse response = authService.register(request);

        // Then
        assertThat(response.username()).isEqualTo("caseuser");
        assertThat(response.email()).isEqualTo("caseuser@example.com");
        assertThat(response.fullName()).isEqualTo("Case User");
        assertThat(response.roles()).containsExactly("ROLE_USER");
        assertThat(response.status()).isEqualTo("ACTIVE");

        // Verify
        verify(appUserRepository).existsByUsername("caseuser");
        verify(appUserRepository).existsByEmail("caseuser@example.com");
        verify(passwordEncoder).encode("password123");

        verify(authMapper).toEntity(
                any(UUID.class),
                eq("caseuser"),
                eq("caseuser@example.com"),
                eq("Case User"),
                eq("encoded-password"),
                eq("ROLE_USER")
        );

        verify(appUserRepository).save(user);
        verify(authAuditService).publishRegisterAuditAsync("caseuser", "caseuser@example.com", "ROLE_USER");
        verify(authMapper).toRegisteredUserResponse(user);
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "caseuser",
                "caseuser@example.com",
                "password123",
                "Case User",
                UserRole.ROLE_USER
        );

        when(appUserRepository.existsByUsername("caseuser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Username already exists");

        // Verify
        verify(appUserRepository).existsByUsername("caseuser");
        verify(appUserRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(appUserRepository, never()).save(any(AppUserEntity.class));
        verify(authMapper, never()).toEntity(any(UUID.class), anyString(), anyString(), anyString(), anyString(), anyString());
        verify(authAuditService, never()).publishRegisterAuditAsync(anyString(), anyString(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "caseuser",
                "caseuser@example.com",
                "password123",
                "Case User",
                UserRole.ROLE_USER
        );

        when(appUserRepository.existsByUsername("caseuser")).thenReturn(false);
        when(appUserRepository.existsByEmail("caseuser@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email already exists");

        // Verify
        verify(appUserRepository).existsByUsername("caseuser");
        verify(appUserRepository).existsByEmail("caseuser@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(appUserRepository, never()).save(any(AppUserEntity.class));
        verify(authMapper, never()).toEntity(any(UUID.class), anyString(), anyString(), anyString(), anyString(), anyString());
        verify(authAuditService, never()).publishRegisterAuditAsync(anyString(), anyString(), anyString());

    }

    @Test
    void shouldLoginUserAndCreateTokenResponse() {
        // Given
        LoginRequest request = new LoginRequest("demo", "password");
        AppUserEntity user = activeUser();

        when(appUserRepository.findByUsername("demo")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(jwtProperties.expirationMinutes()).thenReturn(120L);
        when(jwtTokenService.generateToken("demo", user.getRoles())).thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TokenResponse response = authService.login(request);

        // Then
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.roles()).containsExactly("ROLE_USER");

        // Verify
        verify(appUserRepository).findByUsername("demo");
        verify(passwordEncoder).matches("password", "encoded-password");
        verify(authAuditService).publishLoginSuccessAuditAsync("demo");
        verify(authAuditService, never()).publishLoginFailureAuditAsync(anyString(), anyString());
        verify(jwtProperties).expirationMinutes();
        verify(jwtTokenService).generateToken("demo", user.getRoles());
        verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));

    }

    @Test
    void shouldThrowExceptionWhenLoginUserDoesNotExist() {
        // Given
        LoginRequest request = new LoginRequest("missing", "password");

        when(appUserRepository.findByUsername("missing")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid username/email or password");

        // Verify
        verify(appUserRepository).findByUsername("missing");
        verify(authAuditService).publishLoginFailureAuditAsync("missing", "Invalid username/email or password");
        verify(authAuditService, never()).publishLoginSuccessAuditAsync(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenService, never()).generateToken(anyString(), any());
        verify(refreshTokenRepository, never()).save(any(RefreshTokenEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenLoginUserIsNotActive() {
        // Given
        LoginRequest request = new LoginRequest("demo", "password");

        AppUserEntity user = Mockito.spy(activeUser());
        Mockito.doReturn(false).when(user).isActive();

        when(appUserRepository.findByUsername("demo")).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("User is not active");

        // Verify
        verify(appUserRepository).findByUsername("demo");
        verify(user).isActive();
        verify(authAuditService).publishLoginFailureAuditAsync("demo", "User is not active");
        verify(authAuditService, never()).publishLoginSuccessAuditAsync(anyString());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenService, never()).generateToken(anyString(), any());
        verify(refreshTokenRepository, never()).save(any(RefreshTokenEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenLoginPasswordIsInvalid() {
        // Given
        LoginRequest request = new LoginRequest("demo", "wrong-password");
        AppUserEntity user = activeUser();

        when(appUserRepository.findByUsername("demo")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid username/email or password");

        // Verify
        verify(appUserRepository).findByUsername("demo");
        verify(passwordEncoder).matches("wrong-password", "encoded-password");
        verify(authAuditService).publishLoginFailureAuditAsync("demo", "Invalid username/email or password");
        verify(authAuditService, never()).publishLoginSuccessAuditAsync(anyString());
        verify(jwtTokenService, never()).generateToken(anyString(), any());
        verify(refreshTokenRepository, never()).save(any(RefreshTokenEntity.class));
    }

    @Test
    void shouldRefreshTokenAndRevokeOldRefreshToken() {
        // Given
        String rawRefreshToken = "old-refresh-token";
        String refreshTokenHash = TokenHashUtils.sha256(rawRefreshToken);

        AppUserEntity user = activeUser();

        RefreshTokenEntity existingRefreshToken = new RefreshTokenEntity(
                UUID.randomUUID(),
                user,
                refreshTokenHash,
                Instant.now().plusSeconds(3600)
        );

        RefreshTokenRequest request = new RefreshTokenRequest(rawRefreshToken);

        when(refreshTokenRepository.findByTokenHash(refreshTokenHash)).thenReturn(Optional.of(existingRefreshToken));
        when(jwtProperties.expirationMinutes()).thenReturn(120L);
        when(jwtTokenService.generateToken("demo", user.getRoles())).thenReturn("new-access-token");
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TokenResponse response = authService.refreshToken(request);

        // Then
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(existingRefreshToken.isRevoked()).isTrue();

        // Verify
        verify(refreshTokenRepository).findByTokenHash(refreshTokenHash);
        verify(authAuditService).publishRefreshTokenAuditAsync("demo");
        verify(jwtProperties).expirationMinutes();
        verify(jwtTokenService).generateToken("demo", user.getRoles());
        verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenIsInvalid() {
        // Given
        String rawRefreshToken = "invalid-refresh-token";
        String refreshTokenHash = TokenHashUtils.sha256(rawRefreshToken);

        RefreshTokenRequest request = new RefreshTokenRequest(rawRefreshToken);

        when(refreshTokenRepository.findByTokenHash(refreshTokenHash)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid refresh token");

        // Verify
        verify(refreshTokenRepository).findByTokenHash(refreshTokenHash);
        verify(authAuditService, never()).publishRefreshTokenAuditAsync(anyString());
        verify(jwtTokenService, never()).generateToken(anyString(), any());
        verify(refreshTokenRepository, never()).save(any(RefreshTokenEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenIsRevoked() {
        // Given
        String rawRefreshToken = "revoked-refresh-token";
        String refreshTokenHash = TokenHashUtils.sha256(rawRefreshToken);

        AppUserEntity user = activeUser();

        RefreshTokenEntity refreshToken = new RefreshTokenEntity(
                UUID.randomUUID(),
                user,
                refreshTokenHash,
                Instant.now().plusSeconds(3600)
        );
        refreshToken.revoke();

        RefreshTokenRequest request = new RefreshTokenRequest(rawRefreshToken);

        when(refreshTokenRepository.findByTokenHash(refreshTokenHash)).thenReturn(Optional.of(refreshToken));

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Refresh token is expired or revoked");

        // Verify
        verify(refreshTokenRepository).findByTokenHash(refreshTokenHash);
        verify(authAuditService, never()).publishRefreshTokenAuditAsync(anyString());
        verify(jwtTokenService, never()).generateToken(anyString(), any());
        verify(refreshTokenRepository, never()).save(any(RefreshTokenEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenIsExpired() {
        // Given
        String rawRefreshToken = "expired-refresh-token";
        String refreshTokenHash = TokenHashUtils.sha256(rawRefreshToken);

        AppUserEntity user = activeUser();

        RefreshTokenEntity refreshToken = new RefreshTokenEntity(
                UUID.randomUUID(),
                user,
                refreshTokenHash,
                Instant.now().minusSeconds(60)
        );

        RefreshTokenRequest request = new RefreshTokenRequest(rawRefreshToken);

        when(refreshTokenRepository.findByTokenHash(refreshTokenHash)).thenReturn(Optional.of(refreshToken));

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Refresh token is expired or revoked");

        // Verify
        verify(refreshTokenRepository).findByTokenHash(refreshTokenHash);
        verify(authAuditService, never()).publishRefreshTokenAuditAsync(anyString());
        verify(jwtTokenService, never()).generateToken(anyString(), any());
        verify(refreshTokenRepository, never()).save(any(RefreshTokenEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenUserIsNotActive() {
        // Given
        String rawRefreshToken = "inactive-user-refresh-token";
        String refreshTokenHash = TokenHashUtils.sha256(rawRefreshToken);

        AppUserEntity user = Mockito.spy(activeUser());
        Mockito.doReturn(false).when(user).isActive();

        RefreshTokenEntity refreshToken = new RefreshTokenEntity(
                UUID.randomUUID(),
                user,
                refreshTokenHash,
                Instant.now().plusSeconds(3600)
        );

        RefreshTokenRequest request = new RefreshTokenRequest(rawRefreshToken);

        when(refreshTokenRepository.findByTokenHash(refreshTokenHash)).thenReturn(Optional.of(refreshToken));

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("User is not active");

        // Verify
        verify(refreshTokenRepository).findByTokenHash(refreshTokenHash);
        verify(user).isActive();
        verify(authAuditService, never()).publishRefreshTokenAuditAsync(anyString());
        verify(jwtTokenService, never()).generateToken(anyString(), any());
        verify(refreshTokenRepository, never()).save(any(RefreshTokenEntity.class));
    }

    @Test
    void shouldGenerateForgotPasswordToken() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("demo@example.com");
        AppUserEntity user = activeUser();

        when(appUserRepository.findByEmail("demo@example.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetTokenEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ForgotPasswordResponse response = authService.forgotPassword(request);

        // Then
        assertThat(response.message())
                .isEqualTo("Password reset token was generated. In a production system this token should be sent by email.");
        assertThat(response.resetToken()).isNotBlank();
        assertThat(response.expiresAt()).isAfter(Instant.now());

        // Verify
        verify(appUserRepository).findByEmail("demo@example.com");
        verify(passwordResetTokenRepository).save(any(PasswordResetTokenEntity.class));
        verify(authAuditService).publishForgotPasswordAuditAsync("demo@example.com");
    }

    @Test
    void shouldThrowExceptionWhenForgotPasswordUserDoesNotExist() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("missing@example.com");

        when(appUserRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.forgotPassword(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        // Verify
        verify(appUserRepository).findByEmail("missing@example.com");
        verify(passwordResetTokenRepository, never()).save(any(PasswordResetTokenEntity.class));
        verify(authAuditService, never()).publishForgotPasswordAuditAsync(anyString());
    }

    @Test
    void shouldThrowExceptionWhenForgotPasswordUserIsNotActive() {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("demo@example.com");

        AppUserEntity user = Mockito.spy(activeUser());
        Mockito.doReturn(false).when(user).isActive();

        when(appUserRepository.findByEmail("demo@example.com")).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> authService.forgotPassword(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("User is not active");

        // Verify
        verify(appUserRepository).findByEmail("demo@example.com");
        verify(user).isActive();
        verify(passwordResetTokenRepository, never()).save(any(PasswordResetTokenEntity.class));
        verify(authAuditService, never()).publishForgotPasswordAuditAsync(anyString());
    }

    @Test
    void shouldResetPasswordAndRevokeActiveRefreshTokens() {
        // Given
        String rawResetToken = "reset-token";
        String resetTokenHash = TokenHashUtils.sha256(rawResetToken);

        AppUserEntity user = activeUser();

        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity(
                UUID.randomUUID(),
                user,
                resetTokenHash,
                Instant.now().plusSeconds(900)
        );

        ResetPasswordRequest request = new ResetPasswordRequest(rawResetToken, "newPassword123");

        when(passwordResetTokenRepository.findByTokenHash(resetTokenHash)).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("newPassword123")).thenReturn("new-encoded-password");

        // When
        LogoutResponse response = authService.resetPassword(request);

        // Then
        assertThat(response.message())
                .isEqualTo("Password was reset successfully. Active refresh tokens were revoked.");
        assertThat(user.getPassword()).isEqualTo("new-encoded-password");
        assertThat(resetToken.isUsed()).isTrue();

        // Verify
        verify(passwordResetTokenRepository).findByTokenHash(resetTokenHash);
        verify(passwordEncoder).encode("newPassword123");
        verify(refreshTokenRepository).revokeAllActiveTokensForUser(eq(USER_ID), any(Instant.class));
        verify(authAuditService).publishResetPasswordAuditAsync("demo");
    }

    @Test
    void shouldThrowExceptionWhenResetTokenIsInvalid() {
        // Given
        String rawResetToken = "invalid-reset-token";
        String resetTokenHash = TokenHashUtils.sha256(rawResetToken);

        ResetPasswordRequest request = new ResetPasswordRequest(rawResetToken, "newPassword123");

        when(passwordResetTokenRepository.findByTokenHash(resetTokenHash)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid password reset token");

        // Verify
        verify(passwordResetTokenRepository).findByTokenHash(resetTokenHash);
        verify(passwordEncoder, never()).encode(anyString());
        verify(refreshTokenRepository, never()).revokeAllActiveTokensForUser(any(UUID.class), any(Instant.class));
        verify(authAuditService, never()).publishResetPasswordAuditAsync(anyString());
    }

    @Test
    void shouldThrowExceptionWhenPasswordResetTokenIsAlreadyUsed() {
        // Given
        String rawResetToken = "used-reset-token";
        String resetTokenHash = TokenHashUtils.sha256(rawResetToken);

        AppUserEntity user = activeUser();

        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity(
                UUID.randomUUID(),
                user,
                resetTokenHash,
                Instant.now().plusSeconds(900)
        );
        resetToken.markUsed();

        ResetPasswordRequest request = new ResetPasswordRequest(rawResetToken, "newPassword123");

        when(passwordResetTokenRepository.findByTokenHash(resetTokenHash)).thenReturn(Optional.of(resetToken));

        // When & Then
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Password reset token is expired or already used");

        // Verify
        verify(passwordResetTokenRepository).findByTokenHash(resetTokenHash);
        verify(passwordEncoder, never()).encode(anyString());
        verify(refreshTokenRepository, never()).revokeAllActiveTokensForUser(any(UUID.class), any(Instant.class));
        verify(authAuditService, never()).publishResetPasswordAuditAsync(anyString());
    }

    @Test
    void shouldThrowExceptionWhenPasswordResetTokenIsExpired() {
        // Given
        String rawResetToken = "expired-reset-token";
        String resetTokenHash = TokenHashUtils.sha256(rawResetToken);

        AppUserEntity user = activeUser();

        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity(
                UUID.randomUUID(),
                user,
                resetTokenHash,
                Instant.now().minusSeconds(60)
        );

        ResetPasswordRequest request = new ResetPasswordRequest(rawResetToken, "newPassword123");

        when(passwordResetTokenRepository.findByTokenHash(resetTokenHash)).thenReturn(Optional.of(resetToken));

        // When & Then
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Password reset token is expired or already used");

        // Verify
        verify(passwordResetTokenRepository).findByTokenHash(resetTokenHash);
        verify(passwordEncoder, never()).encode(anyString());
        verify(refreshTokenRepository, never()).revokeAllActiveTokensForUser(any(UUID.class), any(Instant.class));
        verify(authAuditService, never()).publishResetPasswordAuditAsync(anyString());
    }

    @Test
    void shouldLogoutAndRevokeRefreshToken() {
        // Given
        String rawRefreshToken = "refresh-token";
        String refreshTokenHash = TokenHashUtils.sha256(rawRefreshToken);

        AppUserEntity user = activeUser();

        RefreshTokenEntity refreshToken = new RefreshTokenEntity(
                UUID.randomUUID(),
                user,
                refreshTokenHash,
                Instant.now().plusSeconds(3600)
        );

        LogoutRequest request = new LogoutRequest(rawRefreshToken);

        when(refreshTokenRepository.findByTokenHash(refreshTokenHash)).thenReturn(Optional.of(refreshToken));

        // When
        LogoutResponse response = authService.logout(request);

        // Then
        assertThat(response.message()).isEqualTo("Logout completed successfully");
        assertThat(refreshToken.isRevoked()).isTrue();

        // Verify
        verify(refreshTokenRepository).findByTokenHash(refreshTokenHash);
        verify(authAuditService).publishLogoutAuditAsync("demo");
    }

    @Test
    void shouldLogoutWhenRefreshTokenIsAlreadyRevoked() {
        // Given
        String rawRefreshToken = "already-revoked-refresh-token";
        String refreshTokenHash = TokenHashUtils.sha256(rawRefreshToken);

        AppUserEntity user = activeUser();

        RefreshTokenEntity refreshToken = new RefreshTokenEntity(
                UUID.randomUUID(),
                user,
                refreshTokenHash,
                Instant.now().plusSeconds(3600)
        );
        refreshToken.revoke();

        LogoutRequest request = new LogoutRequest(rawRefreshToken);

        when(refreshTokenRepository.findByTokenHash(refreshTokenHash)).thenReturn(Optional.of(refreshToken));

        // When
        LogoutResponse response = authService.logout(request);

        // Then
        assertThat(response.message()).isEqualTo("Logout completed successfully");
        assertThat(refreshToken.isRevoked()).isTrue();

        // Verify
        verify(refreshTokenRepository).findByTokenHash(refreshTokenHash);
        verify(authAuditService).publishLogoutAuditAsync("demo");
    }

    @Test
    void shouldThrowExceptionWhenLogoutRefreshTokenIsInvalid() {
        // Given
        String rawRefreshToken = "invalid-refresh-token";
        String refreshTokenHash = TokenHashUtils.sha256(rawRefreshToken);

        LogoutRequest request = new LogoutRequest(rawRefreshToken);

        when(refreshTokenRepository.findByTokenHash(refreshTokenHash)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.logout(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid refresh token");

        // Verify
        verify(refreshTokenRepository).findByTokenHash(refreshTokenHash);
        verify(authAuditService, never()).publishLogoutAuditAsync(anyString());
    }

    private static AppUserEntity activeUser() {
        return new AppUserEntity(
                USER_ID,
                "demo",
                "demo@example.com",
                "Demo User",
                "encoded-password",
                "ROLE_USER"
        );
    }
}