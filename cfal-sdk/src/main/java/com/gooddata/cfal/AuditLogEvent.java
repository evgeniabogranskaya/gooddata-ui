/**
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gooddata.util.ISODateTimeSerializer;
import org.joda.time.DateTime;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Audit Event emitted from a client application
 */
public class AuditLogEvent {

    private final AuditLogEventType type;

    private final String userLogin;

    private final String userIp;

    private final String domainId;

    private String component;

    @JsonSerialize(using = ISODateTimeSerializer.class)
    private final DateTime occurred;

    private final boolean success;

    public AuditLogEvent(final AuditLogEventType type, final boolean success) {
        // todo GdcCallContext
        this(type, null, null, null, success);
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
        this.userLogin = notEmpty(userLogin, "user login");
        this.userIp = notEmpty(userIp, "user ip");
        this.domainId = notEmpty(domainId, "domain id");
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

    @JsonProperty("domain")
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
}
