/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * ETL process AuditLogEvent
 */
public class ETLProcessAuditLogEvent extends ProjectAuditLogEvent {

    private static final String PROCESS = "process";

    public ETLProcessAuditLogEvent(final String type,
                                   final String userLogin,
                                   final String userIp,
                                   final String domainId,
                                   final boolean success,
                                   final String projectUri,
                                   final String processUri) {
        super(type, userLogin, userIp, domainId, projectUri, success);
        addLink(PROCESS, processUri);
    }

    /**
     * Create audit log event and fill some attributes from GdcCallContext
     *
     * @param type      event type
     * @param success   was this event successful
     * @param processUri ETL process uri
     */
    public ETLProcessAuditLogEvent(final String type, final boolean success, final String processUri) {
        super(type, success);
        addLink(PROCESS, processUri);
    }

    /**
     *
     * @return ETL process uri
     */
    @JsonIgnore
    public String getProcess() {
        return getLink(PROCESS);
    }
}
