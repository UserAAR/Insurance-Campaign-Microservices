package com.casestudy.insurancecampaign.log.repository;

import com.casestudy.insurancecampaign.log.model.document.AuthAuditLogDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Elasticsearch repository for authentication audit log documents.
 */
public interface AuthAuditLogRepository extends ElasticsearchRepository<AuthAuditLogDocument, String> {
}