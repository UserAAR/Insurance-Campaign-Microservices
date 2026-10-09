package com.casestudy.insurancecampaign.campaign.controller;

import com.casestudy.insurancecampaign.campaign.dto.CampaignHistoryResponse;
import com.casestudy.insurancecampaign.campaign.dto.CampaignResponse;
import com.casestudy.insurancecampaign.campaign.dto.CampaignPageResponse;
import com.casestudy.insurancecampaign.campaign.dto.CampaignStatisticsResponse;
import com.casestudy.insurancecampaign.campaign.dto.CreateCampaignRequest;
import com.casestudy.insurancecampaign.campaign.mapper.CampaignMapper;
import com.casestudy.insurancecampaign.campaign.service.CampaignService;
import com.casestudy.insurancecampaign.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/campaigns")
public class CampaignController {

    private final CampaignService campaignService;
    private final CampaignMapper campaignMapper;

    public CampaignController(CampaignService campaignService, CampaignMapper campaignMapper) {
        this.campaignService = campaignService;
        this.campaignMapper = campaignMapper;
    }

    @Operation(summary = "Create insurance campaign. ROLE_ADMIN only.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CampaignResponse> create(@Valid @RequestBody CreateCampaignRequest request) {
        return ApiResponse.success(campaignMapper.toResponse(campaignService.create(request)));
    }

    @Operation(summary = "Get campaign by id. ROLE_USER and ROLE_ADMIN are allowed.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ApiResponse<CampaignResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success(campaignMapper.toResponse(campaignService.getById(id)));
    }

    @Operation(summary = "List all campaigns with pagination and sorting. ROLE_USER and ROLE_ADMIN are allowed.")
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ApiResponse<CampaignPageResponse> getAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.success(CampaignPageResponse.from(campaignService.getAll(pageable).map(campaignMapper::toResponse)));
    }

    @Operation(summary = "Approve PENDING_APPROVAL campaign. ROLE_ADMIN only.")
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CampaignResponse> approve(@PathVariable UUID id) {
        return ApiResponse.success(campaignMapper.toResponse(campaignService.approve(id)));
    }

    @Operation(summary = "Deactivate ACTIVE or PENDING_APPROVAL campaign. ROLE_ADMIN only.")
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CampaignResponse> deactivate(@PathVariable UUID id) {
        return ApiResponse.success(campaignMapper.toResponse(campaignService.deactivate(id)));
    }

    @Operation(summary = "List campaign status history. ROLE_ADMIN only.")
    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CampaignHistoryResponse>> history(@PathVariable UUID id) {
        return ApiResponse.success(campaignService.getHistory(id).stream().map(campaignMapper::toHistoryResponse).toList());
    }

    @Operation(summary = "Campaign statistics by status. ROLE_ADMIN only. Also exposed by dashboard-service through gRPC.")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CampaignStatisticsResponse> statistics() {
        return ApiResponse.success(new CampaignStatisticsResponse(campaignService.getStatistics()));
    }
}
