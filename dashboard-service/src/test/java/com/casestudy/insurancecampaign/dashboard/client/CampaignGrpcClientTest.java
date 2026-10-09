package com.casestudy.insurancecampaign.dashboard.client;

import com.casestudy.insurancecampaign.dashboard.base.AbstractBaseServiceTest;
import com.casestudy.insurancecampaign.dashboard.dto.DashboardCampaignHistoryResponse;
import com.casestudy.insurancecampaign.dashboard.dto.DashboardStatisticsResponse;
import com.casestudy.insurancecampaign.grpc.CampaignGrpcServiceGrpc;
import com.casestudy.insurancecampaign.grpc.CampaignHistoryItem;
import com.casestudy.insurancecampaign.grpc.CampaignHistoryRequest;
import com.casestudy.insurancecampaign.grpc.CampaignHistoryResponse;
import com.casestudy.insurancecampaign.grpc.CampaignStatisticsRequest;
import com.casestudy.insurancecampaign.grpc.CampaignStatisticsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CampaignGrpcClientTest extends AbstractBaseServiceTest {

    private CampaignGrpcClient campaignGrpcClient;

    @Mock
    private CampaignGrpcServiceGrpc.CampaignGrpcServiceBlockingStub campaignStub;

    @BeforeEach
    void setUp() {
        campaignGrpcClient = new CampaignGrpcClient();

        ReflectionTestUtils.setField(
                campaignGrpcClient,
                "campaignStub",
                campaignStub
        );

    }

    @Test
    void shouldReturnDashboardStatisticsWithVisibleStatusesOnly() {

        // Given
        CampaignStatisticsResponse.Builder responseBuilder = CampaignStatisticsResponse.newBuilder();

        responseBuilder.addCountsBuilder()
                .setStatus("PENDING_APPROVAL")
                .setCount(1L);

        responseBuilder.addCountsBuilder()
                .setStatus("ACTIVE")
                .setCount(2L);

        responseBuilder.addCountsBuilder()
                .setStatus("DEACTIVE")
                .setCount(3L);

        responseBuilder.addCountsBuilder()
                .setStatus("DUPLICATE")
                .setCount(99L);

        CampaignStatisticsResponse grpcResponse = responseBuilder.build();

        // When
        when(campaignStub.getCampaignStatistics(any(CampaignStatisticsRequest.class)))
                .thenReturn(grpcResponse);

        // Then
        DashboardStatisticsResponse result = campaignGrpcClient.getStatistics();

        assertThat(result).isNotNull();
        assertThat(result.counts()).hasSize(3);
        assertThat(result.counts()).containsEntry("PENDING_APPROVAL", 1L);
        assertThat(result.counts()).containsEntry("ACTIVE", 2L);
        assertThat(result.counts()).containsEntry("DEACTIVE", 3L);
        assertThat(result.counts()).doesNotContainKey("DUPLICATE");

        // Verify
        verify(campaignStub).getCampaignStatistics(any(CampaignStatisticsRequest.class));

    }

    @Test
    void shouldReturnZeroCountsWhenGrpcStatisticsResponseIsEmpty() {
        // Given
        CampaignStatisticsResponse grpcResponse = CampaignStatisticsResponse.newBuilder()
                .build();

        when(campaignStub.getCampaignStatistics(any(CampaignStatisticsRequest.class)))
                .thenReturn(grpcResponse);

        // When
        DashboardStatisticsResponse result = campaignGrpcClient.getStatistics();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.counts()).hasSize(3);
        assertThat(result.counts()).containsEntry("PENDING_APPROVAL", 0L);
        assertThat(result.counts()).containsEntry("ACTIVE", 0L);
        assertThat(result.counts()).containsEntry("DEACTIVE", 0L);

        // Verify
        verify(campaignStub).getCampaignStatistics(any(CampaignStatisticsRequest.class));
    }

    @Test
    void shouldReturnZeroForMissingVisibleStatuses() {

        // Given
        CampaignStatisticsResponse.Builder responseBuilder = CampaignStatisticsResponse.newBuilder();

        responseBuilder.addCountsBuilder()
                .setStatus("ACTIVE")
                .setCount(5L);

        CampaignStatisticsResponse grpcResponse = responseBuilder.build();

        // When
        when(campaignStub.getCampaignStatistics(any(CampaignStatisticsRequest.class)))
                .thenReturn(grpcResponse);

        // Then
        DashboardStatisticsResponse result = campaignGrpcClient.getStatistics();

        assertThat(result).isNotNull();
        assertThat(result.counts()).hasSize(3);
        assertThat(result.counts()).containsEntry("PENDING_APPROVAL", 0L);
        assertThat(result.counts()).containsEntry("ACTIVE", 5L);
        assertThat(result.counts()).containsEntry("DEACTIVE", 0L);

        // Verify
        verify(campaignStub).getCampaignStatistics(any(CampaignStatisticsRequest.class));

    }

    @Test
    void shouldReturnCampaignHistoryByCampaignId() {

        // Given
        UUID campaignId = UUID.randomUUID();

        CampaignHistoryResponse grpcResponse = CampaignHistoryResponse.newBuilder()
                .addItems(CampaignHistoryItem.newBuilder()
                        .setPreviousStatus("PENDING_APPROVAL")
                        .setNewStatus("ACTIVE")
                        .setReason("Campaign approved")
                        .setChangedAt("2026-07-25T15:00:00Z")
                        .build())
                .build();

        // When
        when(campaignStub.getCampaignHistory(any(CampaignHistoryRequest.class)))
                .thenReturn(grpcResponse);

        // Then
        DashboardCampaignHistoryResponse result = campaignGrpcClient.getCampaignHistory(campaignId);

        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);

        assertThat(result.items().getFirst().previousStatus()).isEqualTo("PENDING_APPROVAL");
        assertThat(result.items().getFirst().newStatus()).isEqualTo("ACTIVE");
        assertThat(result.items().getFirst().reason()).isEqualTo("Campaign approved");
        assertThat(result.items().getFirst().changedAt()).isEqualTo(Instant.parse("2026-07-25T15:00:00Z"));

        // Verify
        verify(campaignStub).getCampaignHistory(argThat(request ->
                request.getCampaignId().equals(campaignId.toString())
        ));

    }

    @Test
    void shouldConvertBlankPreviousStatusToNullWhenReturningCampaignHistory() {

        // Given
        UUID campaignId = UUID.randomUUID();

        CampaignHistoryResponse grpcResponse = CampaignHistoryResponse.newBuilder()
                .addItems(CampaignHistoryItem.newBuilder()
                        .setPreviousStatus("")
                        .setNewStatus("PENDING_APPROVAL")
                        .setReason("Campaign created")
                        .setChangedAt("2026-07-25T14:00:00Z")
                        .build())
                .build();

        // When
        when(campaignStub.getCampaignHistory(any(CampaignHistoryRequest.class)))
                .thenReturn(grpcResponse);

        // Then
        DashboardCampaignHistoryResponse result = campaignGrpcClient.getCampaignHistory(campaignId);

        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);

        assertThat(result.items().getFirst().previousStatus()).isNull();
        assertThat(result.items().getFirst().newStatus()).isEqualTo("PENDING_APPROVAL");
        assertThat(result.items().getFirst().reason()).isEqualTo("Campaign created");
        assertThat(result.items().getFirst().changedAt()).isEqualTo(Instant.parse("2026-07-25T14:00:00Z"));

        // Verify
        verify(campaignStub).getCampaignHistory(argThat(request ->
                request.getCampaignId().equals(campaignId.toString())
        ));

    }

    @Test
    void shouldConvertWhitespacePreviousStatusToNullWhenReturningCampaignHistory() {

        // Given
        UUID campaignId = UUID.randomUUID();

        CampaignHistoryResponse grpcResponse = CampaignHistoryResponse.newBuilder()
                .addItems(CampaignHistoryItem.newBuilder()
                        .setPreviousStatus("   ")
                        .setNewStatus("PENDING_APPROVAL")
                        .setReason("Campaign created")
                        .setChangedAt("2026-07-25T14:00:00Z")
                        .build())
                .build();

        // When
        when(campaignStub.getCampaignHistory(any(CampaignHistoryRequest.class)))
                .thenReturn(grpcResponse);

        // Then
        DashboardCampaignHistoryResponse result = campaignGrpcClient.getCampaignHistory(campaignId);

        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);

        assertThat(result.items().getFirst().previousStatus()).isNull();
        assertThat(result.items().getFirst().newStatus()).isEqualTo("PENDING_APPROVAL");
        assertThat(result.items().getFirst().reason()).isEqualTo("Campaign created");
        assertThat(result.items().getFirst().changedAt()).isEqualTo(Instant.parse("2026-07-25T14:00:00Z"));

        // Verify
        verify(campaignStub).getCampaignHistory(argThat(request ->
                request.getCampaignId().equals(campaignId.toString())
        ));

    }
}