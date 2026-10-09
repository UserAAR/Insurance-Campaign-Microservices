package com.casestudy.insurancecampaign.auth.model.enums;

/**
 * Represents the lifecycle status of an application user.
 *
 * <p>
 * User status is used to determine whether an account can authenticate
 * and perform operations in the system.
 * </p>
 */
public enum UserStatus {

    /**
     * Active user account.
     */
    ACTIVE,

    /**
     * Passive user account.
     *
     * <p>
     * Passive users are not considered active for authentication or business operations.
     * </p>
     */
    PASSIVE,

    /**
     * Deleted user account.
     *
     * <p>
     * Deleted users are logically removed from active usage.
     * </p>
     */
    DELETED

}
