package com.casestudy.insurancecampaign.common.api;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a standardized error response returned by the API.
 *
 * <p>This record is used to return consistent error details for business errors,
 * validation errors, authentication errors, authorization errors, and unexpected
 * system errors.</p>
 *
 * @param code             application-specific error code
 * @param message          human-readable error message
 * @param status           HTTP status code
 * @param path             request path where the error occurred
 * @param validationErrors field-level validation errors, if any
 * @param timestamp        time when the error response was created
 */
public record ApiError(
        String code,
        String message,
        int status,
        String path,
        Map<String, String> validationErrors,
        Instant timestamp
) {

    /**
     * Creates a standard API error response without validation details.
     *
     * @param code    application-specific error code
     * @param message human-readable error message
     * @param status  HTTP status code
     * @param path    request path where the error occurred
     * @return a new {@link ApiError} instance
     */
    public static ApiError of(String code, String message, int status, String path) {
        return new ApiError(code, message, status, path, Map.of(), Instant.now());
    }

    /**
     * Creates a validation error response with field-level validation details.
     *
     * <p>The error code is fixed as {@code VALIDATION_ERROR} because this factory
     * method is specifically used for request validation failures.</p>
     *
     * @param message          human-readable validation error message
     * @param status           HTTP status code
     * @param path             request path where the validation error occurred
     * @param validationErrors field-level validation error messages
     * @return a new {@link ApiError} instance for validation failures
     */
    public static ApiError validation(String message, int status, String path, Map<String, String> validationErrors) {
        return new ApiError("VALIDATION_ERROR", message, status, path, validationErrors, Instant.now());
    }

}
