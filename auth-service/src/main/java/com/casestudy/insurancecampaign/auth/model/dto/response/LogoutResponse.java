package com.casestudy.insurancecampaign.auth.model.dto.response;

/**
 * Response payload returned after logout or password reset operations.
 *
 * @param message informational response message
 */
public record LogoutResponse(
        String message
) { }
