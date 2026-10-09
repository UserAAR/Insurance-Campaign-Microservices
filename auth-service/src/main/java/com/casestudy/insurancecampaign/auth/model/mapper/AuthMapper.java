package com.casestudy.insurancecampaign.auth.model.mapper;

import com.casestudy.insurancecampaign.auth.model.domain.AppUser;
import com.casestudy.insurancecampaign.auth.model.domain.PasswordResetToken;
import com.casestudy.insurancecampaign.auth.model.domain.RefreshToken;
import com.casestudy.insurancecampaign.auth.model.dto.response.RegisteredUserResponse;
import com.casestudy.insurancecampaign.auth.model.entity.AppUserEntity;
import com.casestudy.insurancecampaign.auth.model.entity.PasswordResetTokenEntity;
import com.casestudy.insurancecampaign.auth.model.entity.RefreshTokenEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting auth persistence entities into domain and DTO models.
 */
@Mapper(componentModel = "spring")
public interface AuthMapper {

    /**
     * Creates an application user entity from normalized registration values.
     *
     * @param id unique user identifier
     * @param username normalized username
     * @param email normalized email address
     * @param fullName trimmed full name
     * @param encodedPassword encoded password
     * @param roles comma-separated role values
     * @return application user entity
     */
    default AppUserEntity toEntity(
            java.util.UUID id,
            String username,
            String email,
            String fullName,
            String encodedPassword,
            String roles
    ) {
        return new AppUserEntity(id, username, email, fullName, encodedPassword, roles);
    }

    /**
     * Converts an application user entity to a domain object.
     *
     * @param entity application user entity
     * @return application user domain object
     */
    AppUser toDomain(AppUserEntity entity);

    /**
     * Converts a refresh token entity to a domain object.
     *
     * @param entity refresh token entity
     * @return refresh token domain object
     */
    RefreshToken toDomain(RefreshTokenEntity entity);

    /**
     * Converts a password reset token entity to a domain object.
     *
     * @param entity password reset token entity
     * @return password reset token domain object
     */
    PasswordResetToken toDomain(PasswordResetTokenEntity entity);

    /**
     * Converts an application user entity to the registration response DTO.
     *
     * @param entity application user entity
     * @return registered user response DTO
     */
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    RegisteredUserResponse toRegisteredUserResponse(AppUserEntity entity);

    /**
     * Converts an application user domain object to the registration response DTO.
     *
     * @param domain application user domain object
     * @return registered user response DTO
     */
    @Mapping(target = "status", expression = "java(domain.status().name())")
    RegisteredUserResponse toRegisteredUserResponse(AppUser domain);

}

