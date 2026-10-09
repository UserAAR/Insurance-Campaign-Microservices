package com.casestudy.insurancecampaign.dashboard.dto;

import java.util.Map;

/**
 * Dashboard response containing campaign counts by status.
 *
 * @param counts campaign status counts
 */
public record DashboardStatisticsResponse(Map<String, Long> counts) { }
