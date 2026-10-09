package com.casestudy.insurancecampaign.common.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a campaign status changes.
 *
 * <p>This event is typically sent to Kafka so that other services can react to
 * campaign lifecycle changes such as approval, deactivation, or other status
 * transitions.</p>
 *
 * @param campaignId     unique campaign identifier
 * @param category       campaign category
 * @param previousStatus previous campaign status
 * @param newStatus      new campaign status
 * @param reason         reason of the status change
 * @param occurredAt     time when the status change occurred
 */
public record CampaignStatusChangedEvent(
        UUID campaignId,
        String category,
        String previousStatus,
        String newStatus,
        String reason,
        Instant occurredAt
) { }
