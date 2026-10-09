package com.casestudy.insurancecampaign.campaign.entity;

import com.casestudy.insurancecampaign.campaign.domain.CampaignStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "campaign_status_histories")
public class CampaignStatusHistoryEntity {
    @Id
    private UUID id;

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private CampaignStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 30)
    private CampaignStatus newStatus;

    @Column(nullable = false, length = 200)
    private String reason;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    protected CampaignStatusHistoryEntity() {
    }

    public CampaignStatusHistoryEntity(UUID campaignId, CampaignStatus previousStatus, CampaignStatus newStatus, String reason) {
        this.id = UUID.randomUUID();
        this.campaignId = campaignId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason;
        this.changedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getCampaignId() { return campaignId; }
    public CampaignStatus getPreviousStatus() { return previousStatus; }
    public CampaignStatus getNewStatus() { return newStatus; }
    public String getReason() { return reason; }
    public Instant getChangedAt() { return changedAt; }
}
