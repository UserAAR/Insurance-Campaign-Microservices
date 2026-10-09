package com.casestudy.insurancecampaign.log.service;

import com.casestudy.insurancecampaign.log.model.document.AuthAuditLogDocument;
import com.casestudy.insurancecampaign.log.model.dto.request.AuthAuditLogRequest;
import com.casestudy.insurancecampaign.log.repository.AuthAuditLogRepository;
import org.springframework.stereotype.Service;

/**
 * Service responsible for saving authentication audit logs into Elasticsearch.
 */
@Service
public class AuthAuditLogService {

    private final AuthAuditLogRepository authAuditLogRepository;

    public AuthAuditLogService(AuthAuditLogRepository authAuditLogRepository) {
        this.authAuditLogRepository = authAuditLogRepository;
    }

    /**
     * Saves an authentication audit log document into Elasticsearch.
     *
     * @param request authentication audit log request
     */
    public void save(AuthAuditLogRequest request) {
        AuthAuditLogDocument document = new AuthAuditLogDocument(
                request.serviceName(),
                request.eventType(),
                request.usernameOrEmail(),
                request.status(),
                request.message(),
                request.occurredAt()
        );

        authAuditLogRepository.save(document);
    }
}