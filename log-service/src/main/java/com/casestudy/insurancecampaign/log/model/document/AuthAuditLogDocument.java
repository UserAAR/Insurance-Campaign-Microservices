package com.casestudy.insurancecampaign.log.model.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.UUID;

/**
 * Elasticsearch document representing authentication audit logs received from auth-service.
 */
@Document(indexName = "auth-audit-logs")
public class AuthAuditLogDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String serviceName;

    @Field(type = FieldType.Keyword)
    private String eventType;

    @Field(type = FieldType.Keyword)
    private String usernameOrEmail;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Text)
    private String message;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant occurredAt;

    protected AuthAuditLogDocument() {
    }

    public AuthAuditLogDocument(
            String serviceName,
            String eventType,
            String usernameOrEmail,
            String status,
            String message,
            Instant occurredAt
    ) {
        this.id = UUID.randomUUID().toString();
        this.serviceName = serviceName;
        this.eventType = eventType;
        this.usernameOrEmail = usernameOrEmail;
        this.status = status;
        this.message = message;
        this.occurredAt = occurredAt;
    }

    public String getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getEventType() {
        return eventType;
    }

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}