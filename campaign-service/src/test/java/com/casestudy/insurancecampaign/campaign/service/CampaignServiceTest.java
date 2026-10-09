package com.casestudy.insurancecampaign.campaign.service;

import com.casestudy.insurancecampaign.campaign.domain.CampaignCategory;
import com.casestudy.insurancecampaign.campaign.domain.CampaignStatus;
import com.casestudy.insurancecampaign.campaign.dto.CreateCampaignRequest;
import com.casestudy.insurancecampaign.campaign.entity.CampaignEntity;
import com.casestudy.insurancecampaign.campaign.mapper.CampaignMapper;
import com.casestudy.insurancecampaign.campaign.repository.CampaignRepository;
import com.casestudy.insurancecampaign.campaign.repository.CampaignStatusHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {

    @Mock
    CampaignRepository campaignRepository;

    @Mock
    CampaignStatusHistoryRepository historyRepository;

    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;

    CampaignMapper campaignMapper = new CampaignMapper();

    @Test
    void shouldCreateLifeCampaignAsActive() {
        CampaignService service = new CampaignService(campaignRepository, historyRepository, campaignMapper, kafkaTemplate, "campaign-events");
        CreateCampaignRequest request = new CreateCampaignRequest("Hayat Sigorta 2026", "Hayat sigortası için özel kampanya açıklaması", CampaignCategory.LIFE);
        when(campaignRepository.existsByCategoryAndTitleAndDescriptionAndStatusNot(CampaignCategory.LIFE, request.title(), request.description(), CampaignStatus.DUPLICATE))
                .thenReturn(false);
        when(campaignRepository.saveAndFlush(any(CampaignEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var campaign = service.create(request);

        assertThat(campaign.status()).isEqualTo(CampaignStatus.ACTIVE);
    }

    @Test
    void shouldCreateTssCampaignAsPendingApproval() {
        CampaignService service = new CampaignService(campaignRepository, historyRepository, campaignMapper, kafkaTemplate, "campaign-events");
        CreateCampaignRequest request = new CreateCampaignRequest("TSS Sigorta 2026", "Tamamlayıcı sağlık sigortası kampanya açıklaması", CampaignCategory.TSS);
        when(campaignRepository.existsByCategoryAndTitleAndDescriptionAndStatusNot(CampaignCategory.TSS, request.title(), request.description(), CampaignStatus.DUPLICATE))
                .thenReturn(false);
        when(campaignRepository.saveAndFlush(any(CampaignEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var campaign = service.create(request);

        assertThat(campaign.status()).isEqualTo(CampaignStatus.PENDING_APPROVAL);
    }

    @Test
    void shouldRejectDuplicateCampaignWithoutCreatingNewRow() {
        CampaignService service = new CampaignService(campaignRepository, historyRepository, campaignMapper, kafkaTemplate, "campaign-events");
        CreateCampaignRequest request = new CreateCampaignRequest("TSS Sigorta 2026", "Tamamlayıcı sağlık sigortası kampanya açıklaması", CampaignCategory.TSS);
        when(campaignRepository.existsByCategoryAndTitleAndDescriptionAndStatusNot(CampaignCategory.TSS, request.title(), request.description(), CampaignStatus.DUPLICATE))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(com.casestudy.insurancecampaign.common.exception.DuplicateCampaignException.class)
                .hasMessageContaining("Campaign already exists");

        verify(campaignRepository, never()).save(any(CampaignEntity.class));
        verify(campaignRepository, never()).saveAndFlush(any(CampaignEntity.class));
        verify(historyRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }
}
