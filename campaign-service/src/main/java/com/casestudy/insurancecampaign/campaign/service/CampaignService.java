package com.casestudy.insurancecampaign.campaign.service;

import com.casestudy.insurancecampaign.campaign.domain.Campaign;
import com.casestudy.insurancecampaign.campaign.domain.CampaignCategory;
import com.casestudy.insurancecampaign.campaign.domain.CampaignStatus;
import com.casestudy.insurancecampaign.campaign.dto.CreateCampaignRequest;
import com.casestudy.insurancecampaign.campaign.entity.CampaignEntity;
import com.casestudy.insurancecampaign.campaign.entity.CampaignStatusHistoryEntity;
import com.casestudy.insurancecampaign.campaign.mapper.CampaignMapper;
import com.casestudy.insurancecampaign.campaign.repository.CampaignRepository;
import com.casestudy.insurancecampaign.campaign.repository.CampaignStatusHistoryRepository;
import com.casestudy.insurancecampaign.common.event.CampaignStatusChangedEvent;
import com.casestudy.insurancecampaign.common.exception.DuplicateCampaignException;
import com.casestudy.insurancecampaign.common.exception.InvalidStatusTransitionException;
import com.casestudy.insurancecampaign.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CampaignService {
    private final CampaignRepository campaignRepository;
    private final CampaignStatusHistoryRepository historyRepository;
    private final CampaignMapper campaignMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String campaignEventsTopic;

    public CampaignService(
            CampaignRepository campaignRepository,
            CampaignStatusHistoryRepository historyRepository,
            CampaignMapper campaignMapper,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${app.kafka.topics.campaign-events:campaign-events}") String campaignEventsTopic
    ) {
        this.campaignRepository = campaignRepository;
        this.historyRepository = historyRepository;
        this.campaignMapper = campaignMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.campaignEventsTopic = campaignEventsTopic;
    }

    @Transactional
    @CacheEvict(value = "campaign-statistics", allEntries = true)
    public Campaign create(CreateCampaignRequest request) {
        rejectIfDuplicateExists(request);

        CampaignStatus status = resolveInitialStatus(request.category());
        CampaignEntity entity = new CampaignEntity(
                UUID.randomUUID(),
                request.title(),
                request.description(),
                request.category(),
                status,
                null
        );

        try {
            CampaignEntity saved = campaignRepository.saveAndFlush(entity);
            saveHistory(saved.getId(), null, saved.getStatus(), "Campaign created");
            publishStatusChange(saved.getId(), saved.getCategory(), null, saved.getStatus(), "CREATED");
            return campaignMapper.toDomain(saved);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateCampaignException(duplicateMessage(request));
        }
    }

    @Transactional(readOnly = true)
    public Campaign getById(UUID id) {
        return campaignRepository.findById(id)
                .map(campaignMapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Campaign> getAll(Pageable pageable) {
        return campaignRepository.findAll(pageable).map(campaignMapper::toDomain);
    }

    @Transactional
    @CacheEvict(value = "campaign-statistics", allEntries = true)
    public Campaign approve(UUID id) {
        CampaignEntity entity = findEntity(id);
        ensureNotDuplicate(entity);
        if (entity.getStatus() != CampaignStatus.PENDING_APPROVAL) {
            throw new InvalidStatusTransitionException("Only PENDING_APPROVAL campaigns can be approved");
        }
        return changeStatus(entity, CampaignStatus.ACTIVE, "Campaign approved");
    }

    @Transactional
    @CacheEvict(value = "campaign-statistics", allEntries = true)
    public Campaign deactivate(UUID id) {
        CampaignEntity entity = findEntity(id);
        ensureNotDuplicate(entity);
        if (entity.getStatus() != CampaignStatus.ACTIVE && entity.getStatus() != CampaignStatus.PENDING_APPROVAL) {
            throw new InvalidStatusTransitionException("Only ACTIVE or PENDING_APPROVAL campaigns can be deactivated");
        }
        return changeStatus(entity, CampaignStatus.DEACTIVE, "Campaign deactivated");
    }

    @Transactional(readOnly = true)
    public List<CampaignStatusHistoryEntity> getHistory(UUID id) {
        if (!campaignRepository.existsById(id)) {
            throw new ResourceNotFoundException("Campaign not found: " + id);
        }
        return historyRepository.findByCampaignIdOrderByChangedAtAsc(id);
    }

    @Transactional(readOnly = true)
    @Cacheable("campaign-statistics")
    public Map<String, Long> getStatistics() {
        Map<CampaignStatus, Long> result = new EnumMap<>(CampaignStatus.class);
        for (CampaignStatus status : CampaignStatus.values()) {
            result.put(status, 0L);
        }
        campaignRepository.countByStatus().forEach(item -> result.put(item.getStatus(), item.getCount()));
        return result.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(entry -> entry.getKey().name(), Map.Entry::getValue));
    }

    private CampaignStatus resolveInitialStatus(CampaignCategory category) {
        return switch (category) {
            case LIFE -> CampaignStatus.ACTIVE;
            case TSS, OSS, OTHER -> CampaignStatus.PENDING_APPROVAL;
        };
    }

    private void rejectIfDuplicateExists(CreateCampaignRequest request) {
        boolean exists = campaignRepository.existsByCategoryAndTitleAndDescriptionAndStatusNot(
                request.category(),
                request.title(),
                request.description(),
                CampaignStatus.DUPLICATE
        );
        if (exists) {
            throw new DuplicateCampaignException(duplicateMessage(request));
        }
    }

    private String duplicateMessage(CreateCampaignRequest request) {
        return "Campaign already exists with same category, title and description. category=%s, title=%s"
                .formatted(request.category(), request.title());
    }

    private CampaignEntity findEntity(UUID id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + id));
    }

    private Campaign changeStatus(CampaignEntity entity, CampaignStatus targetStatus, String reason) {
        CampaignStatus previousStatus = entity.getStatus();
        entity.changeStatus(targetStatus);
        CampaignEntity saved = campaignRepository.save(entity);
        saveHistory(saved.getId(), previousStatus, targetStatus, reason);
        publishStatusChange(saved.getId(), saved.getCategory(), previousStatus, targetStatus, reason);
        return campaignMapper.toDomain(saved);
    }

    private void ensureNotDuplicate(CampaignEntity entity) {
        if (entity.getStatus() == CampaignStatus.DUPLICATE) {
            throw new DuplicateCampaignException("Duplicate campaigns cannot be updated");
        }
    }

    private void saveHistory(UUID campaignId, CampaignStatus previousStatus, CampaignStatus newStatus, String reason) {
        historyRepository.save(new CampaignStatusHistoryEntity(campaignId, previousStatus, newStatus, reason));
    }

    private void publishStatusChange(UUID campaignId, CampaignCategory category, CampaignStatus previousStatus, CampaignStatus newStatus, String reason) {
        CampaignStatusChangedEvent event = new CampaignStatusChangedEvent(
                campaignId,
                category.name(),
                previousStatus == null ? null : previousStatus.name(),
                newStatus.name(),
                reason,
                Instant.now()
        );
        kafkaTemplate.send(campaignEventsTopic, campaignId.toString(), event);
    }
}
