package com.casestudy.insurancecampaign.log.service;

import com.casestudy.insurancecampaign.common.event.SlowRequestLogEvent;
import com.casestudy.insurancecampaign.log.model.document.SlowRequestDocument;
import com.casestudy.insurancecampaign.log.repository.SlowRequestLogRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.stream.StreamSupport;

/**
 * Kafka consumer for slow request log events.
 */
@Service
public class SlowRequestLogConsumer {

    private static final Logger log = LoggerFactory.getLogger(SlowRequestLogConsumer.class);

    private final SlowRequestLogRepository repository;

    public SlowRequestLogConsumer(SlowRequestLogRepository repository) {
        this.repository = repository;
    }

    /**
     * Consumes a slow request event and saves it to Elasticsearch.
     *
     * @param event slow request log event
     */
    @KafkaListener(topics = "${app.kafka.topics.log-events:log-events}", groupId = "log-service")
    public void consume(SlowRequestLogEvent event) {
        repository.save(new SlowRequestDocument(
                event.serviceName(),
                event.method(),
                event.path(),
                event.status(),
                event.durationMs(),
                event.traceId(),
                event.headers(),
                event.occurredAt()
        ));
    }

    /**
     * Consumes failed slow request events from DLQ and logs them for inspection.
     *
     * @param record DLQ Kafka record
     */
    @KafkaListener(topics = "${app.kafka.topics.log-events:log-events}.DLQ", groupId = "log-service-dlq")
    public void consumeDlq(ConsumerRecord<String, Object> record) {
        log.error(
                "DLQ message received. topic={}, partition={}, offset={}, key={}, value={}, headers={}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                record.value(),
                headersAsString(record)
        );
    }

    private String headersAsString(ConsumerRecord<String, Object> record) {
        return StreamSupport.stream(record.headers().spliterator(), false)
                .map(this::headerAsString)
                .toList()
                .toString();
    }

    private String headerAsString(Header header) {
        String value = header.value() == null
                ? null
                : new String(header.value(), StandardCharsets.UTF_8);

        return header.key() + "=" + value;

    }

}
