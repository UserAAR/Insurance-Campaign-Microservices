package com.casestudy.insurancecampaign.dashboard.controller;

import com.casestudy.insurancecampaign.common.api.ApiResponse;
import com.casestudy.insurancecampaign.dashboard.client.CampaignGrpcClient;
import com.casestudy.insurancecampaign.dashboard.dto.DashboardCampaignHistoryResponse;
import com.casestudy.insurancecampaign.dashboard.dto.DashboardStatisticsResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for dashboard campaign data.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final CampaignGrpcClient campaignGrpcClient;

    /**
     * Creates a dashboard controller.
     *
     * @param campaignGrpcClient gRPC client used to read campaign data
     */
    public DashboardController(CampaignGrpcClient campaignGrpcClient) {
        this.campaignGrpcClient = campaignGrpcClient;
    }

    /**
     * Returns classified campaign statistics. ROLE_ADMIN only.
     *
     * @return dashboard statistics response
     */
    @Operation(summary = "Classified campaign statistics by status")
    @GetMapping("/classifieds/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DashboardStatisticsResponse> getClassifiedStatistics() {
        return ApiResponse.success(campaignGrpcClient.getStatistics());
    }

    /**
     * Returns campaign status history by campaign id. ROLE_ADMIN only.
     *
     * @param campaignId campaign id
     * @return dashboard campaign history response
     */
    @Operation(summary = "Campaign status history by campaign id")
    @GetMapping("/campaigns/{campaignId}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DashboardCampaignHistoryResponse> getCampaignHistory(@PathVariable UUID campaignId) {
        return ApiResponse.success(campaignGrpcClient.getCampaignHistory(campaignId));
    }

}
