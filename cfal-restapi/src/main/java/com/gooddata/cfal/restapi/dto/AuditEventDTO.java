/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gooddata.util.ISODateTimeDeserializer;
import com.gooddata.util.ISODateTimeSerializer;
import org.joda.time.DateTime;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Audit event DTO
 */
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@JsonTypeName(AuditEventDTO.ROOT_NODE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditEventDTO {

    public static final String DOMAIN_AUDIT_URI = "/gdc/audit";
    public static final String USER_URI = DOMAIN_AUDIT_URI + "/events";
    public static final String ADMIN_URI = DOMAIN_AUDIT_URI + "/admin/events";

    static final String ROOT_NODE = "event";

    private final String id;

    private final String domain;

    private final String userId;

    private final DateTime realTimeOccurrence; //time event happened at component

    private final DateTime timestamp; //time of insertion to mongo

    @JsonCreator
    public AuditEventDTO(@JsonProperty("id") String id,
                         @JsonProperty("domain") String domain,
                         @JsonProperty("userId") String userId,
                         @JsonProperty("realTimeOccurrence") @JsonDeserialize(using = ISODateTimeDeserializer.class) DateTime realTimeOccurrence,
                         @JsonProperty("timestamp") @JsonDeserialize(using = ISODateTimeDeserializer.class) DateTime timestamp) {
        this.id = notEmpty(id);
        this.domain = notEmpty(domain);
        this.userId = notEmpty(userId);
        this.realTimeOccurrence = notNull(realTimeOccurrence);
        this.timestamp = notNull(timestamp);
    }

    public String getId() {
        return id;
    }

    public String getDomain() {
        return domain;
    }

    public String getUserId() {
        return userId;
    }

    @JsonSerialize(using = ISODateTimeSerializer.class)
    public DateTime getRealTimeOccurrence() {
        return realTimeOccurrence;
    }

    @JsonSerialize(using = ISODateTimeSerializer.class)
    public DateTime getTimestamp() {
        return timestamp;
    }

}
