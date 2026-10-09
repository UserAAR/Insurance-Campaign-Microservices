package com.casestudy.insurancecampaign.log.repository;

import com.casestudy.insurancecampaign.log.model.document.SlowRequestDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Elasticsearch repository for slow request logs.
 */
public interface SlowRequestLogRepository extends ElasticsearchRepository<SlowRequestDocument, String> {
}
