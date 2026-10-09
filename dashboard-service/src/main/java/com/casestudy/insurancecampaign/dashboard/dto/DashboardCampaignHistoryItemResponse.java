package com.casestudy.insurancecampaign.dashboard.dto;

import java.time.Instant;

/**
 * Dashboard response item representing one campaign status history record.
 *
 * @param previousStatus previous campaign status
 * @param newStatus new campaign status
 * @param reason status change reason
 * @param changedAt status change timestamp
 */
public record DashboardCampaignHistoryItemResponse(
        String previousStatus,
        String newStatus,
        String reason,
        Instant changedAt
) {
}
