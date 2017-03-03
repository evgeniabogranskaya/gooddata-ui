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
import org.springframework.web.util.UriTemplate;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Audit event DTO
 */
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@JsonTypeName(AuditEventDTO.ROOT_NODE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditEventDTO {

    public static final String GDC_URI = "/gdc";
    public static final String USER_URI = GDC_URI + "/account/profile/{userId}/events";
    public static final String ADMIN_URI = GDC_URI + "/domains/{domainId}/events";

    public static final UriTemplate ADMIN_URI_TEMPLATE = new UriTemplate(ADMIN_URI);
    public static final UriTemplate USER_URI_TEMPLATE = new UriTemplate(USER_URI);

    static final String ROOT_NODE = "event";

    private final String id;

    private final String domain;

    private final String userId;

    private final DateTime occurred; //time event happened at component

    private final DateTime recorded; //time of insertion to mongo

    @JsonCreator
    public AuditEventDTO(@JsonProperty("id") String id,
                         @JsonProperty("domain") String domain,
                         @JsonProperty("userId") String userId,
                         @JsonProperty("occurred") @JsonDeserialize(using = ISODateTimeDeserializer.class) DateTime occurred,
                         @JsonProperty("recorded") @JsonDeserialize(using = ISODateTimeDeserializer.class) DateTime recorded) {
        this.id = notEmpty(id, "id can't be empty");
        this.domain = notEmpty(domain, "domain can't be empty");
        this.userId = notEmpty(userId, "userId can't be empty");
        this.occurred = notNull(occurred, "occurred can't be null");
        this.recorded = notNull(recorded, "recorded can't be null");
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
    public DateTime getOccurred() {
        return occurred;
    }

    @JsonSerialize(using = ISODateTimeSerializer.class)
    public DateTime getRecorded() {
        return recorded;
    }

}
