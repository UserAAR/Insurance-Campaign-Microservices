package com.casestudy.insurancecampaign.campaign.repository;

import com.casestudy.insurancecampaign.campaign.entity.CampaignStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CampaignStatusHistoryRepository extends JpaRepository<CampaignStatusHistoryEntity, UUID> {
    List<CampaignStatusHistoryEntity> findByCampaignIdOrderByChangedAtAsc(UUID campaignId);
}
