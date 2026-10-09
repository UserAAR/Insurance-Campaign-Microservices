package com.casestudy.insurancecampaign.campaign.repository;

import com.casestudy.insurancecampaign.campaign.domain.CampaignCategory;
import com.casestudy.insurancecampaign.campaign.domain.CampaignStatus;
import com.casestudy.insurancecampaign.campaign.entity.CampaignEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CampaignRepository extends JpaRepository<CampaignEntity, UUID> {
    boolean existsByCategoryAndTitleAndDescriptionAndStatusNot(
            CampaignCategory category,
            String title,
            String description,
            CampaignStatus excludedStatus
    );

    @Query("select c.status as status, count(c.id) as count from CampaignEntity c group by c.status")
    List<CampaignStatusCountProjection> countByStatus();

    interface CampaignStatusCountProjection {
        CampaignStatus getStatus();
        long getCount();
    }
}
