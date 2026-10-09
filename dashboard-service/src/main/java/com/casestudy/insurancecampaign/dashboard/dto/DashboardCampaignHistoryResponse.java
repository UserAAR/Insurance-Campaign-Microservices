package com.casestudy.insurancecampaign.dashboard.dto;

import java.util.List;

/**
 * Dashboard response containing campaign status history items.
 *
 * @param items campaign status history items
 */
public record DashboardCampaignHistoryResponse(
        List<DashboardCampaignHistoryItemResponse> items
) {
}
