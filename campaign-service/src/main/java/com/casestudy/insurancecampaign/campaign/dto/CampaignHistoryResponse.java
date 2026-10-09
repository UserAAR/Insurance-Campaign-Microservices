package com.casestudy.insurancecampaign.campaign.dto;

import com.casestudy.insurancecampaign.campaign.domain.CampaignStatus;

import java.time.Instant;

public record CampaignHistoryResponse(
        CampaignStatus previousStatus,
        CampaignStatus newStatus,
        String reason,
        Instant changedAt
) { }
