package com.casestudy.insurancecampaign.campaign.dto;

import java.util.Map;

public record CampaignStatisticsResponse(Map<String, Long> counts) { }
