package com.casestudy.insurancecampaign.campaign.mapper;

import com.casestudy.insurancecampaign.campaign.domain.Campaign;
import com.casestudy.insurancecampaign.campaign.dto.CampaignHistoryResponse;
import com.casestudy.insurancecampaign.campaign.dto.CampaignResponse;
import com.casestudy.insurancecampaign.campaign.entity.CampaignEntity;
import com.casestudy.insurancecampaign.campaign.entity.CampaignStatusHistoryEntity;
import org.springframework.stereotype.Component;

@Component
public class CampaignMapper {
    public Campaign toDomain(CampaignEntity entity) {
        return new Campaign(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCategory(),
                entity.getStatus(),
                entity.getDuplicateOf(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public CampaignResponse toResponse(Campaign campaign) {
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

    public CampaignHistoryResponse toHistoryResponse(CampaignStatusHistoryEntity entity) {
        return new CampaignHistoryResponse(entity.getPreviousStatus(), entity.getNewStatus(), entity.getReason(), entity.getChangedAt());
    }
}
