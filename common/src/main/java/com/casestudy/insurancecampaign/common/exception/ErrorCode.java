package com.casestudy.insurancecampaign.common.exception;

/**
 * Defines application-specific error codes used in API error responses.
 *
 * <p>These codes provide a stable error contract for clients. Instead of relying
 * only on HTTP status codes, clients can use these values to understand the
 * exact reason of a failure.</p>
 */
public enum ErrorCode {

    /**
     * Requested resource could not be found.
     */
    RESOURCE_NOT_FOUND,

    /**
     * Request validation failed.
     */
    VALIDATION_ERROR,

    /**
     * A business rule was violated.
     */
    BUSINESS_RULE_VIOLATION,

    /**
     * A duplicate campaign already exists.
     */
    DUPLICATE_CAMPAIGN,

    /**
     * Requested campaign status transition is not allowed.
     */
    INVALID_STATUS_TRANSITION,

    /**
     * Authentication is missing or invalid.
     */
    UNAUTHORIZED,

    /**
     * Authenticated user does not have enough permission.
     */
    FORBIDDEN,

    /**
     * Unexpected internal server error occurred.
     */
    INTERNAL_SERVER_ERROR

}
