package com.casestudy.insurancecampaign.auth.model.domain;

import java.time.Instant;

/**
 * Domain model representing an authentication audit event sent to log-service.
 *
 * @param serviceName source service name
 * @param eventType authentication event type
 * @param usernameOrEmail user identifier involved in the event
 * @param message human-readable audit message
 * @param status event status such as SUCCESS or FAILED
 * @param occurredAt event timestamp
 */
public record AuthAuditLog(
        String serviceName,
        String eventType,
        String usernameOrEmail,
        String message,
        String status,
        Instant occurredAt
) {
}
