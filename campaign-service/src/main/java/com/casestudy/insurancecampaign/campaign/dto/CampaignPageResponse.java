package com.casestudy.insurancecampaign.campaign.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record CampaignPageResponse(
        List<CampaignResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        List<String> sort
) {
    public static CampaignPageResponse from(Page<CampaignResponse> page) {
        List<String> sortOrders = page.getSort().stream()
                .map(order -> order.getProperty() + "," + order.getDirection().name().toLowerCase())
                .toList();
        return new CampaignPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                sortOrders
        );
    }
}
