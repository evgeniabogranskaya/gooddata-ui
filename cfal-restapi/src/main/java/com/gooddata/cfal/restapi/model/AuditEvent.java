/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.model;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;

/**
 * Audit event entity
 */
public class AuditEvent {

    @Id
    private ObjectId id;

    private String domainId;

    private String userLogin;

    private DateTime occurred; //time event happened at component

    private String userIp;

    private boolean success;

    private String type;

    public AuditEvent(final String domainId, final String userLogin, final DateTime occurred, final String userIp, final boolean success, final String type) {
        this.domainId = domainId;
        this.userLogin = userLogin;
        this.occurred = occurred;
        this.userIp = userIp;
        this.success = success;
        this.type = type;
    }

    /**
     * Constructor for testing purposes
     */
    public AuditEvent(final ObjectId id, final String domainId, final String userLogin, final DateTime occurred, final String userIp, final boolean success, final String type) {
        this.id = id;
        this.domainId = domainId;
        this.userLogin = userLogin;
        this.occurred = occurred;
        this.userIp = userIp;
        this.success = success;
        this.type = type;
    }

    AuditEvent() {
    }

    public ObjectId getId() {
        return id;
    }

    public String getDomainId() {
        return domainId;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public DateTime getOccurred() {
        return occurred;
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
}
