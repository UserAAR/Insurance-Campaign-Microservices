package com.casestudy.insurancecampaign.dashboard.client;

import com.casestudy.insurancecampaign.dashboard.dto.DashboardCampaignHistoryItemResponse;
import com.casestudy.insurancecampaign.dashboard.dto.DashboardCampaignHistoryResponse;
import com.casestudy.insurancecampaign.dashboard.dto.DashboardStatisticsResponse;
import com.casestudy.insurancecampaign.grpc.CampaignGrpcServiceGrpc;
import com.casestudy.insurancecampaign.grpc.CampaignHistoryRequest;
import com.casestudy.insurancecampaign.grpc.CampaignStatisticsRequest;
import com.casestudy.insurancecampaign.grpc.CampaignStatisticsResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * gRPC client for reading campaign data from campaign-service.
 */
@Component
public class CampaignGrpcClient {

    private static final List<String> DASHBOARD_VISIBLE_STATUSES = List.of(
            "PENDING_APPROVAL",
            "ACTIVE",
            "DEACTIVE"
    );

    @GrpcClient("campaign-service")
    private CampaignGrpcServiceGrpc.CampaignGrpcServiceBlockingStub campaignStub;

    /**
     * Gets campaign statistics for dashboard view.
     *
     * @return dashboard statistics response
     */
    public DashboardStatisticsResponse getStatistics() {
        var response = campaignStub.getCampaignStatistics(CampaignStatisticsRequest.newBuilder().build());
        return toDashboardStatisticsResponse(response);
    }

    /**
     * Gets campaign status history by campaign id.
     *
     * @param campaignId campaign id
     * @return dashboard campaign history response
     */
    public DashboardCampaignHistoryResponse getCampaignHistory(UUID campaignId) {
        var request = CampaignHistoryRequest.newBuilder()
                .setCampaignId(campaignId.toString())
                .build();

        var response = campaignStub.getCampaignHistory(request);

        List<DashboardCampaignHistoryItemResponse> items = response.getItemsList()
                .stream()
                .map(item -> new DashboardCampaignHistoryItemResponse(
                        blankToNull(item.getPreviousStatus()),
                        item.getNewStatus(),
                        item.getReason(),
                        Instant.parse(item.getChangedAt())
                ))
                .toList();

        return new DashboardCampaignHistoryResponse(items);
    }

    /**
     * Converts gRPC campaign statistics response to dashboard response.
     *
     * @param response gRPC campaign statistics response
     * @return dashboard statistics response
     */
    private DashboardStatisticsResponse toDashboardStatisticsResponse(CampaignStatisticsResponse response) {
        Map<String, Long> counts = new LinkedHashMap<>();
        DASHBOARD_VISIBLE_STATUSES.forEach(status -> counts.put(status, 0L));

        response.getCountsList().forEach(item -> {
            if (counts.containsKey(item.getStatus())) {
                counts.put(item.getStatus(), item.getCount());
            }
        });

        return new DashboardStatisticsResponse(counts);
    }

    /**
     * Converts blank text to null.
     *
     * @param value text value
     * @return null when value is blank, otherwise original value
     */
    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

}
