package com.casestudy.insurancecampaign.auth.model.mapper;

import com.casestudy.insurancecampaign.auth.model.domain.AppUser;
import com.casestudy.insurancecampaign.auth.model.domain.PasswordResetToken;
import com.casestudy.insurancecampaign.auth.model.domain.RefreshToken;
import com.casestudy.insurancecampaign.auth.model.dto.response.RegisteredUserResponse;
import com.casestudy.insurancecampaign.auth.model.entity.AppUserEntity;
import com.casestudy.insurancecampaign.auth.model.entity.PasswordResetTokenEntity;
import com.casestudy.insurancecampaign.auth.model.entity.RefreshTokenEntity;
import com.casestudy.insurancecampaign.auth.model.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthMapperTest {

    private AuthMapper authMapper;

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID REFRESH_TOKEN_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PASSWORD_RESET_TOKEN_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final Instant CREATED_AT = Instant.parse("2026-07-10T10:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-07-10T11:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-07-10T12:00:00Z");
    private static final Instant REVOKED_AT = Instant.parse("2026-07-10T13:00:00Z");
    private static final Instant USED_AT = Instant.parse("2026-07-10T14:00:00Z");

    @BeforeEach
    void setUp() {
        authMapper = new AuthMapperImpl();
    }

    @Test
    void shouldCreateAppUserEntityFromRegistrationValues() {
        // Given
        UUID id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        String username = "caseuser";
        String email = "caseuser@example.com";
        String fullName = "Case User";
        String encodedPassword = "encoded-password";
        String roles = "ROLE_USER";

        // When
        AppUserEntity result = authMapper.toEntity(
                id,
                username,
                email,
                fullName,
                encodedPassword,
                roles
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getUsername()).isEqualTo("caseuser");
        assertThat(result.getEmail()).isEqualTo("caseuser@example.com");
        assertThat(result.getFullName()).isEqualTo("Case User");
        assertThat(result.getPassword()).isEqualTo("encoded-password");
        assertThat(result.getRoles()).containsExactly("ROLE_USER");
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void shouldMapAppUserEntityToDomain() {
        // Given
        AppUserEntity entity = mockAppUserEntity();

        // When
        AppUser result = authMapper.toDomain(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(USER_ID);
        assertThat(result.username()).isEqualTo("demo");
        assertThat(result.email()).isEqualTo("demo@example.com");
        assertThat(result.fullName()).isEqualTo("Demo User");
        assertThat(result.roles()).containsExactly("ROLE_USER", "ROLE_ADMIN");
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.createdAt()).isEqualTo(CREATED_AT);
        assertThat(result.updatedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    void shouldMapAppUserEntityToDomainWhenRolesAreNull() {
        // Given
        AppUserEntity entity = mock(AppUserEntity.class);

        when(entity.getId()).thenReturn(USER_ID);
        when(entity.getUsername()).thenReturn("demo");
        when(entity.getEmail()).thenReturn("demo@example.com");
        when(entity.getFullName()).thenReturn("Demo User");
        when(entity.getRoles()).thenReturn(null);
        when(entity.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(entity.getCreatedAt()).thenReturn(CREATED_AT);
        when(entity.getUpdatedAt()).thenReturn(UPDATED_AT);

        // When
        AppUser result = authMapper.toDomain(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(USER_ID);
        assertThat(result.roles()).isNull();
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void shouldReturnNullWhenAppUserEntityIsNull() {
        // When
        AppUser result = authMapper.toDomain((AppUserEntity) null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldMapRefreshTokenEntityToDomain() {
        // Given
        AppUserEntity userEntity = mockAppUserEntity();

        RefreshTokenEntity entity = mock(RefreshTokenEntity.class);
        when(entity.getId()).thenReturn(REFRESH_TOKEN_ID);
        when(entity.getUser()).thenReturn(userEntity);
        when(entity.getTokenHash()).thenReturn("refresh-token-hash");
        when(entity.getExpiresAt()).thenReturn(EXPIRES_AT);
        when(entity.getCreatedAt()).thenReturn(CREATED_AT);
        when(entity.getRevokedAt()).thenReturn(REVOKED_AT);

        // When
        RefreshToken result = authMapper.toDomain(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(REFRESH_TOKEN_ID);
        assertThat(result.tokenHash()).isEqualTo("refresh-token-hash");
        assertThat(result.expiresAt()).isEqualTo(EXPIRES_AT);
        assertThat(result.createdAt()).isEqualTo(CREATED_AT);
        assertThat(result.revokedAt()).isEqualTo(REVOKED_AT);

        assertThat(result.user()).isNotNull();
        assertThat(result.user().id()).isEqualTo(USER_ID);
        assertThat(result.user().username()).isEqualTo("demo");
        assertThat(result.user().roles()).containsExactly("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void shouldReturnNullWhenRefreshTokenEntityIsNull() {
        // When
        RefreshToken result = authMapper.toDomain((RefreshTokenEntity) null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldMapPasswordResetTokenEntityToDomain() {
        // Given
        AppUserEntity userEntity = mockAppUserEntity();

        PasswordResetTokenEntity entity = mock(PasswordResetTokenEntity.class);
        when(entity.getId()).thenReturn(PASSWORD_RESET_TOKEN_ID);
        when(entity.getUser()).thenReturn(userEntity);
        when(entity.getTokenHash()).thenReturn("password-reset-token-hash");
        when(entity.getExpiresAt()).thenReturn(EXPIRES_AT);
        when(entity.getCreatedAt()).thenReturn(CREATED_AT);
        when(entity.getUsedAt()).thenReturn(USED_AT);

        // When
        PasswordResetToken result = authMapper.toDomain(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(PASSWORD_RESET_TOKEN_ID);
        assertThat(result.tokenHash()).isEqualTo("password-reset-token-hash");
        assertThat(result.expiresAt()).isEqualTo(EXPIRES_AT);
        assertThat(result.createdAt()).isEqualTo(CREATED_AT);
        assertThat(result.usedAt()).isEqualTo(USED_AT);

        assertThat(result.user()).isNotNull();
        assertThat(result.user().id()).isEqualTo(USER_ID);
        assertThat(result.user().username()).isEqualTo("demo");
        assertThat(result.user().roles()).containsExactly("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void shouldReturnNullWhenPasswordResetTokenEntityIsNull() {
        // When
        PasswordResetToken result = authMapper.toDomain((PasswordResetTokenEntity) null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldMapAppUserEntityToRegisteredUserResponse() {
        // Given
        AppUserEntity entity = mockAppUserEntity();

        // When
        RegisteredUserResponse result = authMapper.toRegisteredUserResponse(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(USER_ID);
        assertThat(result.username()).isEqualTo("demo");
        assertThat(result.email()).isEqualTo("demo@example.com");
        assertThat(result.fullName()).isEqualTo("Demo User");
        assertThat(result.roles()).containsExactly("ROLE_USER", "ROLE_ADMIN");
        assertThat(result.status()).isEqualTo("ACTIVE");
        assertThat(result.createdAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void shouldMapAppUserEntityToRegisteredUserResponseWhenRolesAreNull() {
        // Given
        AppUserEntity entity = mock(AppUserEntity.class);

        when(entity.getId()).thenReturn(USER_ID);
        when(entity.getUsername()).thenReturn("demo");
        when(entity.getEmail()).thenReturn("demo@example.com");
        when(entity.getFullName()).thenReturn("Demo User");
        when(entity.getRoles()).thenReturn(null);
        when(entity.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(entity.getCreatedAt()).thenReturn(CREATED_AT);

        // When
        RegisteredUserResponse result = authMapper.toRegisteredUserResponse(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(USER_ID);
        assertThat(result.roles()).isNull();
        assertThat(result.status()).isEqualTo("ACTIVE");
        assertThat(result.createdAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void shouldReturnNullWhenAppUserEntityToRegisteredUserResponseInputIsNull() {
        // When
        RegisteredUserResponse result = authMapper.toRegisteredUserResponse((AppUserEntity) null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldMapAppUserDomainToRegisteredUserResponse() {
        // Given
        AppUser domain = new AppUser(
                USER_ID,
                "demo",
                "demo@example.com",
                "Demo User",
                List.of("ROLE_USER", "ROLE_ADMIN"),
                UserStatus.ACTIVE,
                CREATED_AT,
                UPDATED_AT
        );

        // When
        RegisteredUserResponse result = authMapper.toRegisteredUserResponse(domain);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(USER_ID);
        assertThat(result.username()).isEqualTo("demo");
        assertThat(result.email()).isEqualTo("demo@example.com");
        assertThat(result.fullName()).isEqualTo("Demo User");
        assertThat(result.roles()).containsExactly("ROLE_USER", "ROLE_ADMIN");
        assertThat(result.status()).isEqualTo("ACTIVE");
        assertThat(result.createdAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void shouldMapAppUserDomainToRegisteredUserResponseWhenRolesAreNull() {
        // Given
        AppUser domain = new AppUser(
                USER_ID,
                "demo",
                "demo@example.com",
                "Demo User",
                null,
                UserStatus.ACTIVE,
                CREATED_AT,
                UPDATED_AT
        );

        // When
        RegisteredUserResponse result = authMapper.toRegisteredUserResponse(domain);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(USER_ID);
        assertThat(result.roles()).isNull();
        assertThat(result.status()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldReturnNullWhenAppUserDomainToRegisteredUserResponseInputIsNull() {
        // When
        RegisteredUserResponse result = authMapper.toRegisteredUserResponse((AppUser) null);

        // Then
        assertThat(result).isNull();
    }

    private AppUserEntity mockAppUserEntity() {
        AppUserEntity entity = mock(AppUserEntity.class);

        when(entity.getId()).thenReturn(USER_ID);
        when(entity.getUsername()).thenReturn("demo");
        when(entity.getEmail()).thenReturn("demo@example.com");
        when(entity.getFullName()).thenReturn("Demo User");
        when(entity.getRoles()).thenReturn(List.of("ROLE_USER", "ROLE_ADMIN"));
        when(entity.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(entity.getCreatedAt()).thenReturn(CREATED_AT);
        when(entity.getUpdatedAt()).thenReturn(UPDATED_AT);

        return entity;
    }
}