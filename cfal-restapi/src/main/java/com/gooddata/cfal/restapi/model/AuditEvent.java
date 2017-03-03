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

    private String domain;

    private String userId;

    private DateTime occurred; //time event happened at component

    public AuditEvent(final String domain, final String userId, final DateTime occurred) {
        this.domain = domain;
        this.userId = userId;
        this.occurred = occurred;
    }

    /**
     * Constructor for testing purposes
     */
    public AuditEvent(final ObjectId id, final String domain, final String userId, final DateTime occurred) {
        this.id = id;
        this.domain = domain;
        this.userId = userId;
        this.occurred = occurred;
    }

    AuditEvent() {
    }

    public ObjectId getId() {
        return id;
    }

    public String getDomain() {
        return domain;
    }

    public String getUserId() {
        return userId;
    }

    public DateTime getOccurred() {
        return occurred;
    }
}
