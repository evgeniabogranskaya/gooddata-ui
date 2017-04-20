/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gooddata.util.ISODateTimeDeserializer;
import com.gooddata.util.ISODateTimeSerializer;
import org.joda.time.DateTime;
import org.springframework.web.util.UriTemplate;

import java.util.Map;

/**
 * Audit event DTO
 */
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@JsonTypeName(AuditEventDTO.ROOT_NODE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditEventDTO {

    public static final String GDC_URI = "/gdc";
    public static final String USER_URI = GDC_URI + "/account/profile/{userId}/auditEvents";
    public static final String ADMIN_URI = GDC_URI + "/domains/{domainId}/auditEvents";

    public static final UriTemplate ADMIN_URI_TEMPLATE = new UriTemplate(ADMIN_URI);
    public static final UriTemplate USER_URI_TEMPLATE = new UriTemplate(USER_URI);

    static final String ROOT_NODE = "event";

    private final String id;

    private final String userLogin;

    private final DateTime occurred; //time event happened at component

    private final DateTime recorded; //time of insertion to mongo

    private final String userIp;

    private final boolean success;

    private final String type;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, String> params;

    @JsonCreator
    public AuditEventDTO(@JsonProperty("id") String id,
                         @JsonProperty("userLogin") String userLogin,
                         @JsonProperty("occurred") @JsonDeserialize(using = ISODateTimeDeserializer.class) DateTime occurred,
                         @JsonProperty("recorded") @JsonDeserialize(using = ISODateTimeDeserializer.class) DateTime recorded,
                         @JsonProperty("userIp") String userIp,
                         @JsonProperty("success") boolean success,
                         @JsonProperty("type") String type,
                         @JsonProperty("params") Map<String, String> params) {
        this.id = id;
        this.userLogin = userLogin;
        this.occurred = occurred;
        this.recorded = recorded;
        this.userIp = userIp;
        this.success = success;
        this.type = type;
        this.params = params;
    }

    public String getId() {
        return id;
    }

    public String getUserLogin() {
        return userLogin;
    }

    @JsonSerialize(using = ISODateTimeSerializer.class)
    public DateTime getOccurred() {
        return occurred;
    }

    @JsonSerialize(using = ISODateTimeSerializer.class)
    public DateTime getRecorded() {
        return recorded;
    }

    public String getUserIp() {
        return userIp;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getType() {
        return type;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
