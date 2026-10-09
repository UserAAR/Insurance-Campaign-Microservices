package com.casestudy.insurancecampaign.campaign.dto;

import com.casestudy.insurancecampaign.campaign.domain.CampaignCategory;
import com.casestudy.insurancecampaign.campaign.domain.CampaignStatus;

import java.time.Instant;
import java.util.UUID;

public record CampaignResponse(
        UUID id,
        String title,
        String description,
        CampaignCategory category,
        CampaignStatus status,
        UUID duplicateOf,
        Instant createdAt,
        Instant updatedAt
) { }
