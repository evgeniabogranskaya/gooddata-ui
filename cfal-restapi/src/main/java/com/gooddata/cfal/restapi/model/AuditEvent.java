/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.model;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotNull;

/**
 * Audit event entity
 */
public class AuditEvent {

    @Id
    private ObjectId id;

    @NotNull
    private String domain;

    @NotNull
    private String userId;

    @NotNull
    private DateTime realTimeOccurrence; //time event happened at component

    public AuditEvent(final String domain, final String userId, final DateTime realTimeOccurrence) {
        this.domain = domain;
        this.userId = userId;
        this.realTimeOccurrence = realTimeOccurrence;
    }

    /**
     * Constructor for testing purposes
     */
    public AuditEvent(final ObjectId id, final String domain, final String userId, final DateTime realTimeOccurrence) {
        this.id = id;
        this.domain = domain;
        this.userId = userId;
        this.realTimeOccurrence = realTimeOccurrence;
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

    public DateTime getRealTimeOccurrence() {
        return realTimeOccurrence;
    }
}
