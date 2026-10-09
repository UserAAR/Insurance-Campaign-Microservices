package com.casestudy.insurancecampaign.log.model.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Document(indexName = "slow-request-logs")
public class SlowRequestDocument {
    @Id
    private String id;
    private String serviceName;
    private String method;
    private String path;
    private int status;
    private long durationMs;
    private String traceId;
    private Map<String, String> headers;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant occurredAt;

    protected SlowRequestDocument() {
    }

    public SlowRequestDocument(String serviceName, String method, String path, int status, long durationMs, String traceId, Map<String, String> headers, Instant occurredAt) {
        this.id = UUID.randomUUID().toString();
        this.serviceName = serviceName;
        this.method = method;
        this.path = path;
        this.status = status;
        this.durationMs = durationMs;
        this.traceId = traceId;
        this.headers = headers;
        this.occurredAt = occurredAt;
    }

    public String getId() { return id; }
    public String getServiceName() { return serviceName; }
    public String getMethod() { return method; }
    public String getPath() { return path; }
    public int getStatus() { return status; }
    public long getDurationMs() { return durationMs; }
    public String getTraceId() { return traceId; }
    public Map<String, String> getHeaders() { return headers; }
    public Instant getOccurredAt() { return occurredAt; }
}
