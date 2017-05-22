/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gooddata.context.GdcCallContext;
import com.gooddata.util.ISODateTimeSerializer;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Audit Event emitted from a client application
 */
public class AuditLogEvent {

    private final AuditLogEventType type;

    private final String userLogin;

    private final String userIp;

    private final String domainId;

    private String component;

    @JsonProperty("params")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> params = new HashMap<>();

    @JsonProperty("links")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> links = new HashMap<>();

    @JsonSerialize(using = ISODateTimeSerializer.class)
    private final DateTime occurred;

    private final boolean success;

    /**
     * Create audit log event and fill some attributes from GdcCallContext
     * @param type type
     * @param success was this event successful
     */
    public AuditLogEvent(final AuditLogEventType type, final boolean success) {
        this(type,
             GdcCallContext.getCurrentContext().getUserLogin(),
             GdcCallContext.getCurrentContext().getClientIp(),
             GdcCallContext.getCurrentContext().getDomainId(),
             success);
    }

    /**
     * Create a successful audit log event
     * @param type type
     * @param userLogin user's login name
     * @param userIp user's IP address
     * @param domainId domain ID
     */
    public AuditLogEvent(final AuditLogEventType type, final String userLogin, final String userIp,
                         final String domainId) {
        this(type, userLogin, userIp, domainId, true);
    }


    public AuditLogEvent(final AuditLogEventType type, final String userLogin, final String userIp,
                         final String domainId, final boolean success) {
        this(type, userLogin, userIp, domainId, new DateTime(), success);
    }

    public AuditLogEvent(final AuditLogEventType type, final String userLogin, final String userIp,
                         final String domainId, final DateTime occurred, final boolean success) {
        this.type = notNull(type, "type");
        this.userLogin = userLogin;
        this.userIp = userIp;
        this.domainId = domainId;
        this.occurred = notNull(occurred, "occurred time");
        this.success = success;
    }

    public AuditLogEventType getType() {
        return type;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public String getUserIp() {
        return userIp;
    }

    public String getDomainId() {
        return domainId;
    }

    public DateTime getOccurred() {
        return occurred;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(final String component) {
        this.component = component;
    }

    protected void addParam(final String key, final String value) {
        params.put(key, value);
    }

    protected String getParam(final String key) {
        return params.get(key);
    }

    protected void addLink(final String key, final String value) {
        links.put(key, value);
    }

    protected String getLink(final String key) {
        return links.get(key);
    }

    boolean isValid() {
        return !StringUtils.isBlank(userLogin) && !StringUtils.isBlank(userIp) && !StringUtils.isBlank(domainId);
    }

    @Override
    public String toString() {
        return "AuditLogEvent{" +
                "type=" + type +
                ", userLogin='" + userLogin + '\'' +
                ", userIp='" + userIp + '\'' +
                ", domainId='" + domainId + '\'' +
                ", component='" + component + '\'' +
                ", params=" + params +
                ", occurred=" + occurred +
                ", success=" + success +
                '}';
    }
}
