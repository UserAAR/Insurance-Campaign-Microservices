package com.casestudy.insurancecampaign.log.model.dto.request;

import java.time.Instant;

/**
 * Request payload received from auth-service for authentication audit events.
 *
 * @param serviceName source service name
 * @param eventType authentication event type
 * @param usernameOrEmail user identifier involved in the event
 * @param message audit message
 * @param status event status
 * @param occurredAt event timestamp
 */
public record AuthAuditLogRequest(
        String serviceName,
        String eventType,
        String usernameOrEmail,
        String message,
        String status,
        Instant occurredAt
) {
}
