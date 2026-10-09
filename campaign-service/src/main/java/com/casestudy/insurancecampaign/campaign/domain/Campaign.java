package com.casestudy.insurancecampaign.campaign.domain;

import java.time.Instant;
import java.util.UUID;

public record Campaign(
        UUID id,
        String title,
        String description,
        CampaignCategory category,
        CampaignStatus status,
        UUID duplicateOf,
        Instant createdAt,
        Instant updatedAt
) { }
