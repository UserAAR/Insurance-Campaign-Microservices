package com.casestudy.insurancecampaign.auth.service;

import com.casestudy.insurancecampaign.auth.model.dto.request.*;
import com.casestudy.insurancecampaign.auth.model.dto.response.ForgotPasswordResponse;
import com.casestudy.insurancecampaign.auth.model.dto.response.LogoutResponse;
import com.casestudy.insurancecampaign.auth.model.dto.response.RegisteredUserResponse;
import com.casestudy.insurancecampaign.auth.model.dto.response.TokenResponse;
import com.casestudy.insurancecampaign.auth.model.entity.AppUserEntity;
import com.casestudy.insurancecampaign.auth.model.entity.PasswordResetTokenEntity;
import com.casestudy.insurancecampaign.auth.model.entity.RefreshTokenEntity;
import com.casestudy.insurancecampaign.auth.model.mapper.AuthMapper;
import com.casestudy.insurancecampaign.auth.repository.AppUserRepository;
import com.casestudy.insurancecampaign.auth.repository.PasswordResetTokenRepository;
import com.casestudy.insurancecampaign.auth.repository.RefreshTokenRepository;
import com.casestudy.insurancecampaign.auth.util.StringUtils;
import com.casestudy.insurancecampaign.auth.util.TokenHashUtils;
import com.casestudy.insurancecampaign.common.exception.BusinessException;
import com.casestudy.insurancecampaign.common.exception.ErrorCode;
import com.casestudy.insurancecampaign.common.exception.ResourceNotFoundException;
import com.casestudy.insurancecampaign.security.JwtProperties;
import com.casestudy.insurancecampaign.security.JwtTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for authentication and account-related operations.
 *
 * <p>
 * This service handles user registration, login, refresh-token rotation,
 * forgot-password token generation, password reset, and logout operations.
 * </p>
 */
@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final long refreshTokenExpirationDays;
    private final long passwordResetTokenExpirationMinutes;
    private final AuthAuditService authAuditService;
    private final AuthMapper authMapper;

    /**
     * Creates a new {@link AuthService}.
     *
     * @param appUserRepository repository for application users
     * @param refreshTokenRepository repository for refresh tokens
     * @param passwordResetTokenRepository repository for password reset tokens
     * @param jwtTokenService service used to generate JWT access tokens
     * @param jwtProperties JWT configuration properties
     * @param passwordEncoder password encoder used for hashing and verifying passwords
     * @param authMapper mapper used for DTO, entity, and domain conversions
     * @param authAuditService service used to publish authentication audit events
     * @param refreshTokenExpirationDays refresh token expiration duration in days
     * @param passwordResetTokenExpirationMinutes password reset token expiration duration in minutes
     */
    public AuthService(
            AppUserRepository appUserRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            JwtTokenService jwtTokenService,
            JwtProperties jwtProperties,
            PasswordEncoder passwordEncoder,
            AuthAuditService authAuditService,
            @Value("${app.auth.refresh-token-expiration-days:7}") long refreshTokenExpirationDays,
            @Value("${app.auth.password-reset-token-expiration-minutes:15}") long passwordResetTokenExpirationMinutes, AuthMapper authMapper
    ) {
        this.appUserRepository = appUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
        this.passwordResetTokenExpirationMinutes = passwordResetTokenExpirationMinutes;
        this.authAuditService = authAuditService;
        this.authMapper = authMapper;
    }

    /**
     * Registers a new application user.
     *
     * <p>
     * Username and email values are normalized before duplicate checks.
     * The password is encoded before the user is persisted.
     * </p>
     *
     * @param request registration request
     * @return registered user response
     * @throws BusinessException if username or email already exists
     */
    @Transactional
    public RegisteredUserResponse register(RegisterRequest request) {
        String username = StringUtils.normalize(request.username());
        String email = StringUtils.normalize(request.email());

        if (appUserRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Username already exists");
        }
        if (appUserRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Email already exists");
        }

        AppUserEntity user = authMapper.toEntity(
                UUID.randomUUID(),
                username,
                email,
                request.fullName().trim(),
                passwordEncoder.encode(request.password()),
                request.role().name()
        );
        AppUserEntity savedUser = appUserRepository.save(user);
        authAuditService.publishRegisterAuditAsync(username, email, request.role().name());
        return authMapper.toRegisteredUserResponse(savedUser);
    }

    /**
     * Authenticates a user and creates a token response.
     *
     * <p>
     * A successful login generates a JWT access token and a new refresh token.
     * Login audit information is also published asynchronously.
     * </p>
     *
     * @param request login request containing username/email and password
     * @return token response containing access token and refresh token
     * @throws BusinessException if user is not found, inactive, or password is invalid
     */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        Optional<AppUserEntity> optionalUser = findByUsernameOrEmail(request.usernameOrEmail());
        if (optionalUser.isEmpty()) {
            authAuditService.publishLoginFailureAuditAsync(request.usernameOrEmail(), "Invalid username/email or password");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid username/email or password");
        }

        AppUserEntity user = optionalUser.get();

        if (!user.isActive()) {
            authAuditService.publishLoginFailureAuditAsync(request.usernameOrEmail(), "User is not active");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "User is not active");
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            authAuditService.publishLoginFailureAuditAsync(request.usernameOrEmail(), "Invalid username/email or password");
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid username/email or password");
        }

        authAuditService.publishLoginSuccessAuditAsync(request.usernameOrEmail());
        return createTokenResponse(user);
    }

    /**
     * Rotates a refresh token and creates a new token response.
     *
     * <p>
     * The existing refresh token is revoked after validation and a new refresh
     * token is persisted.
     * </p>
     *
     * @param request refresh-token request
     * @return new token response
     * @throws BusinessException if refresh token is invalid, expired, revoked, or user is inactive
     */
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        RefreshTokenEntity refreshToken = refreshTokenRepository.findByTokenHash(TokenHashUtils.sha256(request.refreshToken()))
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid refresh token"));

        if (refreshToken.isRevoked() || refreshToken.isExpired()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Refresh token is expired or revoked");
        }
        if (!refreshToken.getUser().isActive()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "User is not active");
        }

        refreshToken.revoke();
        authAuditService.publishRefreshTokenAuditAsync(refreshToken.getUser().getUsername());
        return createTokenResponse(refreshToken.getUser());
    }

    /**
     * Generates a password reset token for an active user.
     *
     * <p>
     * The raw reset token is returned in the response, while only its SHA-256 hash
     * is stored in the database.
     * </p>
     *
     * @param request forgot-password request containing username or email
     * @return forgot-password response containing reset token information
     * @throws ResourceNotFoundException if user does not exist
     * @throws BusinessException if user is inactive
     */
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        AppUserEntity user = findByUsernameOrEmail(request.usernameOrEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "User is not active");
        }

        String resetToken = TokenHashUtils.randomToken();
        Instant expiresAt = Instant.now().plusSeconds(passwordResetTokenExpirationMinutes * 60);
        PasswordResetTokenEntity entity = new PasswordResetTokenEntity(
                UUID.randomUUID(),
                user,
                TokenHashUtils.sha256(resetToken),
                expiresAt
        );
        passwordResetTokenRepository.save(entity);
        authAuditService.publishForgotPasswordAuditAsync(request.usernameOrEmail());

        return new ForgotPasswordResponse(
                "Password reset token was generated. In a production system this token should be sent by email.",
                resetToken,
                expiresAt
        );
    }

    /**
     * Resets a user's password by using a valid password reset token.
     *
     * <p>
     * After a successful password reset, the reset token is marked as used and all
     * active refresh tokens of the user are revoked.
     * </p>
     *
     * @param request reset-password request
     * @return logout-style response message
     * @throws BusinessException if reset token is invalid, expired, or already used
     */
    @Transactional
    public LogoutResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetTokenEntity resetToken = passwordResetTokenRepository.findByTokenHash(TokenHashUtils.sha256(request.resetToken()))
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid password reset token"));

        if (resetToken.isUsed() || resetToken.isExpired()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Password reset token is expired or already used");
        }

        AppUserEntity user = resetToken.getUser();
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        resetToken.markUsed();
        refreshTokenRepository.revokeAllActiveTokensForUser(user.getId(), Instant.now());
        authAuditService.publishResetPasswordAuditAsync(user.getUsername());

        return new LogoutResponse("Password was reset successfully. Active refresh tokens were revoked.");
    }

    /**
     * Logs out a user by revoking the provided refresh token.
     *
     * @param request logout request containing refresh token
     * @return logout response message
     * @throws BusinessException if refresh token is invalid
     */
    @Transactional
    public LogoutResponse logout(LogoutRequest request) {
        RefreshTokenEntity refreshToken = refreshTokenRepository.findByTokenHash(TokenHashUtils.sha256(request.refreshToken()))
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid refresh token"));

        if (!refreshToken.isRevoked()) {
            refreshToken.revoke();
        }
        authAuditService.publishLogoutAuditAsync(refreshToken.getUser().getUsername());
        return new LogoutResponse("Logout completed successfully");
    }

    /**
     * Creates a token response for the given user.
     *
     * <p>
     * This method generates a JWT access token, creates a new refresh token,
     * stores the hashed refresh token, and returns token metadata.
     * </p>
     *
     * @param user authenticated user
     * @return token response
     */
    private TokenResponse createTokenResponse(AppUserEntity user) {
        Instant issuedAt = Instant.now();
        Instant accessTokenExpiresAt = issuedAt.plusSeconds(jwtProperties.expirationMinutes() * 60);
        Instant refreshTokenExpiresAt = issuedAt.plusSeconds(refreshTokenExpirationDays * 24 * 60 * 60);

        String accessToken = jwtTokenService.generateToken(user.getUsername(), user.getRoles());
        String refreshToken = TokenHashUtils.randomToken();
        refreshTokenRepository.save(new RefreshTokenEntity(
                UUID.randomUUID(),
                user,
                TokenHashUtils.sha256(refreshToken),
                refreshTokenExpiresAt
        ));

        return new TokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                issuedAt,
                accessTokenExpiresAt,
                refreshTokenExpiresAt,
                user.getRoles()
        );
    }

    /**
     * Finds a user by username or email.
     *
     * <p>
     * The input value is normalized before lookup. If the value contains {@code @},
     * email lookup is used; otherwise username lookup is used.
     * </p>
     *
     * @param usernameOrEmail username or email value
     * @return optional user entity
     */
    private Optional<AppUserEntity> findByUsernameOrEmail(String usernameOrEmail) {
        String normalized = usernameOrEmail.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("@")) {
            return appUserRepository.findByEmail(normalized);
        }
        return appUserRepository.findByUsername(normalized);
    }


}
