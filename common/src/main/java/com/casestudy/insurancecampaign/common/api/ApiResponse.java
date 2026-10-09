package com.casestudy.insurancecampaign.common.api;

import java.time.Instant;

/**
 * Represents a standardized successful API response.
 *
 * <p>This wrapper provides a consistent response structure across all services.
 * It includes a success flag, response data, and response creation timestamp.</p>
 *
 * @param <T>       type of the response data
 * @param success   indicates whether the request was successful
 * @param data      response payload
 * @param timestamp time when the response was created
 */
public record ApiResponse<T>(
        boolean success,
        T data,
        Instant timestamp
) {

    /**
     * Creates a successful API response with the given data.
     *
     * @param data response payload
     * @param <T>  type of the response data
     * @return a new successful {@link ApiResponse} instance
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, Instant.now());
    }

}
