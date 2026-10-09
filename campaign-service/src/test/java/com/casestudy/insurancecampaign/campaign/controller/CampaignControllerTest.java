package com.casestudy.insurancecampaign.campaign.controller;

import com.casestudy.insurancecampaign.campaign.base.AbstractRestControllerTest;
import com.casestudy.insurancecampaign.campaign.domain.Campaign;
import com.casestudy.insurancecampaign.campaign.domain.CampaignCategory;
import com.casestudy.insurancecampaign.campaign.domain.CampaignStatus;
import com.casestudy.insurancecampaign.campaign.dto.CampaignHistoryResponse;
import com.casestudy.insurancecampaign.campaign.dto.CampaignResponse;
import com.casestudy.insurancecampaign.campaign.dto.CreateCampaignRequest;
import com.casestudy.insurancecampaign.campaign.entity.CampaignStatusHistoryEntity;
import com.casestudy.insurancecampaign.campaign.mapper.CampaignMapper;
import com.casestudy.insurancecampaign.campaign.service.CampaignService;
import com.casestudy.insurancecampaign.common.exception.DuplicateCampaignException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CampaignControllerTest extends AbstractRestControllerTest {

    @MockitoBean
    private CampaignService campaignService;

    @MockitoBean
    private CampaignMapper campaignMapper;

    @Test
    void shouldCreateCampaignWithPendingApprovalStatusWhenRoleIsAdmin() throws Exception {

        // Given
        UUID campaignId = UUID.randomUUID();

        CreateCampaignRequest request = new CreateCampaignRequest(
                "TSS Kampanya 2026",
                "TSS kategorisi için yirmi karakterden uzun açıklama",
                CampaignCategory.TSS
        );

        Campaign campaign = campaign(
                campaignId,
                request.title(),
                request.description(),
                request.category(),
                CampaignStatus.PENDING_APPROVAL
        );

        CampaignResponse response = response(campaign);

        // When
        when(campaignService.create(any(CreateCampaignRequest.class))).thenReturn(campaign);
        when(campaignMapper.toResponse(campaign)).thenReturn(response);

        //  Then
        mockMvc.perform(post("/api/v1/campaigns")
                        .header(HttpHeaders.AUTHORIZATION, adminBearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(campaignId.toString()))
                .andExpect(jsonPath("$.data.title").value("TSS Kampanya 2026"))
                .andExpect(jsonPath("$.data.category").value("TSS"))
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"));

        // Verify
        verify(campaignService).create(argThat(argument ->
                argument.title().equals("TSS Kampanya 2026")
                        && argument.description().equals("TSS kategorisi için yirmi karakterden uzun açıklama")
                        && argument.category() == CampaignCategory.TSS
        ));
        verify(campaignMapper).toResponse(campaign);

    }

    @Test
    void shouldRejectCreateCampaignWhenRoleIsUser() throws Exception {

        // Given
        CreateCampaignRequest request = new CreateCampaignRequest(
                "User Kampanya 2026",
                "ROLE_USER kampanya oluşturamaz açıklaması",
                CampaignCategory.TSS
        );

        // When & Then
        mockMvc.perform(post("/api/v1/campaigns")
                        .header(HttpHeaders.AUTHORIZATION, userBearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        // Verify
        verifyNoInteractions(campaignService);
        verifyNoInteractions(campaignMapper);

    }

    @Test
    void shouldRejectDuplicateCampaignWhenRoleIsAdmin() throws Exception {

        // Given
        CreateCampaignRequest request = new CreateCampaignRequest(
                "OSS Kampanya 2026",
                "OSS kategorisi için yirmi karakterden uzun açıklama",
                CampaignCategory.OSS
        );

        // When
        when(campaignService.create(any(CreateCampaignRequest.class)))
                .thenThrow(new DuplicateCampaignException("Campaign already exists"));

        // Then
        mockMvc.perform(post("/api/v1/campaigns")
                        .header(HttpHeaders.AUTHORIZATION, adminBearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_CAMPAIGN"));

        // Verify
        verify(campaignService).create(any(CreateCampaignRequest.class));
        verify(campaignMapper, never()).toResponse(any(Campaign.class));

    }

    @Test
    void shouldGetCampaignByIdWhenRoleIsUser() throws Exception {

        // Given
        UUID campaignId = UUID.randomUUID();

        Campaign campaign = campaign(
                campaignId,
                "Life Campaign 2026",
                "Life campaign için yirmi karakterden uzun açıklama",
                CampaignCategory.LIFE,
                CampaignStatus.ACTIVE
        );

        CampaignResponse response = response(campaign);

        // When
        when(campaignService.getById(campaignId)).thenReturn(campaign);
        when(campaignMapper.toResponse(campaign)).thenReturn(response);

        // Then
        mockMvc.perform(get("/api/v1/campaigns/{id}", campaignId)
                        .header(HttpHeaders.AUTHORIZATION, userBearerToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(campaignId.toString()))
                .andExpect(jsonPath("$.data.title").value("Life Campaign 2026"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // Verify
        verify(campaignService).getById(campaignId);
        verify(campaignMapper).toResponse(campaign);

    }

    @Test
    void shouldGetCampaignByIdWhenRoleIsAdmin() throws Exception {

        // Given
        UUID campaignId = UUID.randomUUID();

        Campaign campaign = campaign(
                campaignId,
                "Admin Campaign 2026",
                "Admin campaign için yirmi karakterden uzun açıklama",
                CampaignCategory.OSS,
                CampaignStatus.ACTIVE
        );

        CampaignResponse response = response(campaign);

        // When
        when(campaignService.getById(campaignId)).thenReturn(campaign);
        when(campaignMapper.toResponse(campaign)).thenReturn(response);

        // Then
        mockMvc.perform(get("/api/v1/campaigns/{id}", campaignId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearerToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(campaignId.toString()))
                .andExpect(jsonPath("$.data.title").value("Admin Campaign 2026"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // Verify
        verify(campaignService).getById(campaignId);
        verify(campaignMapper).toResponse(campaign);

    }

    @Test
    void shouldListCampaignsWithPaginationAndSortingWhenRoleIsUser() throws Exception {

        // Given
        UUID campaignId = UUID.randomUUID();

        Campaign campaign = campaign(
                campaignId,
                "Page Campaign 2026",
                "Pagination sorting için yirmi karakterden uzun açıklama",
                CampaignCategory.LIFE,
                CampaignStatus.ACTIVE
        );

        CampaignResponse response = response(campaign);

        PageRequest pageRequest = PageRequest.of(
                0,
                5,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // When
        when(campaignService.getAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(campaign), pageRequest, 1));

        when(campaignMapper.toResponse(campaign)).thenReturn(response);

        // Then
        mockMvc.perform(get("/api/v1/campaigns")
                        .header(HttpHeaders.AUTHORIZATION, userBearerToken())
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "createdAt,desc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(campaignId.toString()))
                .andExpect(jsonPath("$.data.content[0].title").value("Page Campaign 2026"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        // Verify
        verify(campaignService).getAll(argThat(pageable ->
                pageable.getPageNumber() == 0
                        && pageable.getPageSize() == 5
                        && pageable.getSort().getOrderFor("createdAt") != null
        ));
        verify(campaignMapper).toResponse(campaign);

    }

    @Test
    void shouldApprovePendingApprovalCampaignWhenRoleIsAdmin() throws Exception {

        // Given
        UUID campaignId = UUID.randomUUID();

        Campaign campaign = campaign(
                campaignId,
                "Approve Campaign 2026",
                "Approve campaign için yirmi karakterden uzun açıklama",
                CampaignCategory.TSS,
                CampaignStatus.ACTIVE
        );

        CampaignResponse response = response(campaign);

        // When
        when(campaignService.approve(campaignId)).thenReturn(campaign);
        when(campaignMapper.toResponse(campaign)).thenReturn(response);

        // Then
        mockMvc.perform(patch("/api/v1/campaigns/{id}/approve", campaignId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearerToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(campaignId.toString()))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // Verify
        verify(campaignService).approve(campaignId);
        verify(campaignMapper).toResponse(campaign);

    }

    @Test
    void shouldRejectApproveCampaignWhenRoleIsUser() throws Exception {

        // Given
        UUID campaignId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(patch("/api/v1/campaigns/{id}/approve", campaignId)
                        .header(HttpHeaders.AUTHORIZATION, userBearerToken()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        // Verify
        verifyNoInteractions(campaignService);
        verifyNoInteractions(campaignMapper);

    }

    @Test
    void shouldDeactivateCampaignWhenRoleIsAdmin() throws Exception {

        // Given
        UUID campaignId = UUID.randomUUID();

        Campaign campaign = campaign(
                campaignId,
                "Deactivate Campaign 2026",
                "Deactivate campaign için yirmi karakterden uzun açıklama",
                CampaignCategory.OSS,
                CampaignStatus.DEACTIVE
        );

        CampaignResponse response = response(campaign);

        // When
        when(campaignService.deactivate(campaignId)).thenReturn(campaign);
        when(campaignMapper.toResponse(campaign)).thenReturn(response);

        // Then
        mockMvc.perform(patch("/api/v1/campaigns/{id}/deactivate", campaignId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearerToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(campaignId.toString()))
                .andExpect(jsonPath("$.data.status").value("DEACTIVE"));

        // Verify
        verify(campaignService).deactivate(campaignId);
        verify(campaignMapper).toResponse(campaign);

    }

    @Test
    void shouldRejectDeactivateCampaignWhenRoleIsUser() throws Exception {

        // Given
        UUID campaignId = UUID.randomUUID();

        // Then
        mockMvc.perform(patch("/api/v1/campaigns/{id}/deactivate", campaignId)
                        .header(HttpHeaders.AUTHORIZATION, userBearerToken()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        // Verify
        verifyNoInteractions(campaignService);
        verifyNoInteractions(campaignMapper);

    }

    @Test
    void shouldListCampaignStatusHistoryWhenRoleIsAdmin() throws Exception {

        // Given
        UUID campaignId = UUID.randomUUID();

        CampaignStatusHistoryEntity historyEntity = new CampaignStatusHistoryEntity(
                campaignId,
                CampaignStatus.PENDING_APPROVAL,
                CampaignStatus.ACTIVE,
                "Campaign approved"
        );

        CampaignHistoryResponse historyResponse = new CampaignHistoryResponse(
                CampaignStatus.PENDING_APPROVAL,
                CampaignStatus.ACTIVE,
                "Campaign approved",
                historyEntity.getChangedAt()
        );

        // When
        when(campaignService.getHistory(campaignId)).thenReturn(List.of(historyEntity));
        when(campaignMapper.toHistoryResponse(historyEntity)).thenReturn(historyResponse);

        // Then
        mockMvc.perform(get("/api/v1/campaigns/{id}/history", campaignId)
                        .header(HttpHeaders.AUTHORIZATION, adminBearerToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].previousStatus").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.data[0].newStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data[0].reason").value("Campaign approved"));

        // Verify
        verify(campaignService).getHistory(campaignId);
        verify(campaignMapper).toHistoryResponse(historyEntity);

    }

    @Test
    void shouldRejectListCampaignStatusHistoryWhenRoleIsUser() throws Exception {

        // Given
        UUID campaignId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(get("/api/v1/campaigns/{id}/history", campaignId)
                        .header(HttpHeaders.AUTHORIZATION, userBearerToken()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        // Verify
        verifyNoInteractions(campaignService);
        verifyNoInteractions(campaignMapper);

    }

    @Test
    void shouldReturnCampaignStatisticsWhenRoleIsAdmin() throws Exception {

        // Given
        when(campaignService.getStatistics()).thenReturn(Map.of(
                "ACTIVE", 2L,
                "PENDING_APPROVAL", 1L,
                "DEACTIVE", 1L
        ));

        // When & Then
        mockMvc.perform(get("/api/v1/campaigns/statistics")
                        .header(HttpHeaders.AUTHORIZATION, adminBearerToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.counts.ACTIVE").value(2))
                .andExpect(jsonPath("$.data.counts.PENDING_APPROVAL").value(1))
                .andExpect(jsonPath("$.data.counts.DEACTIVE").value(1));

        // Verify
        verify(campaignService).getStatistics();
        verifyNoInteractions(campaignMapper);

    }

    @Test
    void shouldRejectCampaignStatisticsWhenRoleIsUser() throws Exception {

        // When & Then
        mockMvc.perform(get("/api/v1/campaigns/statistics")
                        .header(HttpHeaders.AUTHORIZATION, userBearerToken()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        // Verify
        verifyNoInteractions(campaignService);
        verifyNoInteractions(campaignMapper);

    }

    private Campaign campaign(
            UUID id,
            String title,
            String description,
            CampaignCategory category,
            CampaignStatus status
    ) {
        Instant now = Instant.parse("2026-07-25T14:00:00Z");

        return new Campaign(
                id,
                title,
                description,
                category,
                status,
                null,
                now,
                now
        );
    }

    private CampaignResponse response(Campaign campaign) {
        return new CampaignResponse(
                campaign.id(),
                campaign.title(),
                campaign.description(),
                campaign.category(),
                campaign.status(),
                campaign.duplicateOf(),
                campaign.createdAt(),
                campaign.updatedAt()
        );
    }

}