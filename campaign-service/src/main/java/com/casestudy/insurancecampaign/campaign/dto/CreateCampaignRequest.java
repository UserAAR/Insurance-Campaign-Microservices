package com.casestudy.insurancecampaign.campaign.dto;

import com.casestudy.insurancecampaign.campaign.domain.CampaignCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCampaignRequest(
        @NotBlank
        @Size(min = 10, max = 50)
        @Pattern(regexp = "^[\\p{L}\\d].*", message = "title must start with a Turkish letter, another letter, or a digit")
        String title,

        @NotBlank
        @Size(min = 20, max = 200)
        String description,

        @NotNull
        CampaignCategory category
) { }
