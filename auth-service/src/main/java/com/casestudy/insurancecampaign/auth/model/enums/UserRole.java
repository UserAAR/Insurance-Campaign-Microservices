package com.casestudy.insurancecampaign.auth.model.enums;

/**
 * Represents application user roles used by the auth-service.
 *
 * <p>
 * Roles are used for authorization decisions across the system.
 * </p>
 */
public enum UserRole {

    /**
     * Standard application user role.
     */
    ROLE_USER,

    /**
     * Administrator role with elevated permissions.
     */
    ROLE_ADMIN

}
