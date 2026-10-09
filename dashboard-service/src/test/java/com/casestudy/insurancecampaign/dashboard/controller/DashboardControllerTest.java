package com.casestudy.insurancecampaign.dashboard.controller;

import com.casestudy.insurancecampaign.dashboard.base.AbstractRestControllerTest;
import com.casestudy.insurancecampaign.dashboard.client.CampaignGrpcClient;
import com.casestudy.insurancecampaign.dashboard.dto.DashboardCampaignHistoryItemResponse;
import com.casestudy.insurancecampaign.dashboard.dto.DashboardCampaignHistoryResponse;
import com.casestudy.insurancecampaign.dashboard.dto.DashboardStatisticsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DashboardControllerTest extends AbstractRestControllerTest {

    @MockitoBean
    private CampaignGrpcClient campaignGrpcClient;

    @Test
    void shouldReturnClassifiedCampaignStatisticsWhenRoleIsAdmin() throws Exception {

        // Given
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("PENDING_APPROVAL", 1L);
        counts.put("ACTIVE", 2L);
        counts.put("DEACTIVE", 3L);

        // When
        when(campaignGrpcClient.getStatistics())
                .thenReturn(new DashboardStatisticsResponse(counts));

        // Then
        mockMvc.perform(get("/api/v1/dashboard/classifieds/statistics")
                        .header(HttpHeaders.AUTHORIZATION, adminBearerToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.counts.PENDING_APPROVAL").value(1))
                .andExpect(jsonPath("$.data.counts.ACTIVE").value(2))
                .andExpect(jsonPath("$.data.counts.DEACTIVE").value(3))
                .andExpect(jsonPath("$.data.counts.DUPLICATE").doesNotExist());

        // Verify
        verify(campaignGrpcClient).getStatistics();

    }

    @Test
    void shouldRejectClassifiedCampaignStatisticsWhenRoleIsUser() throws Exception {

        // When & Then
        mockMvc.perform(get("/api/v1/dashboard/classifieds/statistics")
                        .header(HttpHeaders.AUTHORIZATION, userBearerToken()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        // Verify
        verifyNoInteractions(campaignGrpcClient);

    }

    @Test
    void shouldRejectClassifiedCampaignStatisticsWhenUnauthenticated() throws Exception {

        // When & Then
        mockMvc.perform(get("/api/v1/dashboard/classifieds/statistics"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        // Verify
        verifyNoInteractions(campaignGrpcClient);

    }

    @Test
    void shouldReturnCampaignHistoryWhenRoleIsAdmin() throws Exception {

        // Given
        UUID campaignId = UUID.randomUUID();

        DashboardCampaignHistoryResponse response = new DashboardCampaignHistoryResponse(
                List.of(
                        new DashboardCampaignHistoryItemResponse(
                                null,
                                "PENDING_APPROVAL",
                                "Campaign created",
                                Instant.parse("2026-07-27T09:00:00Z")
                        ),
                        new DashboardCampaignHistoryItemResponse(
                                "PENDING_APPROVAL",
                                "ACTIVE",
                                "Campaign approved",
                                Instant.parse("2026-07-27T10:00:00Z")
                        )
                )
        );

        // When
        when(campaignGrpcClient.getCampaignHistory(campaignId)).thenReturn(response);

        // Then
        mockMvc.perform(get("/api/v1/dashboard/campaigns/{campaignId}/history", campaignId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearerToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].previousStatus").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].newStatus").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.data.items[0].reason").value("Campaign created"))
                .andExpect(jsonPath("$.data.items[0].changedAt").value("2026-07-27T09:00:00Z"))
                .andExpect(jsonPath("$.data.items[1].previousStatus").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.data.items[1].newStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.items[1].reason").value("Campaign approved"))
                .andExpect(jsonPath("$.data.items[1].changedAt").value("2026-07-27T10:00:00Z"));

        // Verify
        verify(campaignGrpcClient).getCampaignHistory(campaignId);

    }

    @Test
    void shouldRejectCampaignHistoryWhenRoleIsUser() throws Exception {

        // Given
        UUID campaignId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(get("/api/v1/dashboard/campaigns/{campaignId}/history", campaignId)
                        .header(HttpHeaders.AUTHORIZATION, userBearerToken()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        // Verify
        verifyNoInteractions(campaignGrpcClient);

    }

    @Test
    void shouldRejectCampaignHistoryWhenUnauthenticated() throws Exception {

        // Given
        UUID campaignId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(get("/api/v1/dashboard/campaigns/{campaignId}/history", campaignId))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        // Verify
        verifyNoInteractions(campaignGrpcClient);
    }

}
