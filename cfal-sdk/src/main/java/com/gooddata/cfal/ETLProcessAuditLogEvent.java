/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gooddata.context.GdcCallContext;

/**
 * ETL process AuditLogEvent
 */
public class ETLProcessAuditLogEvent extends AuditLogEvent {

    private static final String PROJECT = "project";
    private static final String PROCESS = "process";
    private static final String PROJECT_URI_PREFIX = "/gdc/projects/";

    public ETLProcessAuditLogEvent(final AuditLogEventType type,
                                   final String userLogin,
                                   final String userIp,
                                   final String domainId,
                                   final boolean success,
                                   final String project,
                                   final String process) {
        super(type, userLogin, userIp, domainId, success);

        addLink(PROJECT, project);
        addLink(PROCESS, process);
    }

    /**
     * Create audit log event and fill some attributes from GdcCallContext
     *
     * @param type      event type
     * @param success   was this event successful
     * @param process ETL process uri
     */
    public ETLProcessAuditLogEvent(final AuditLogEventType type, final boolean success, final String process) {
        super(type, success);
        addLink(PROJECT, PROJECT_URI_PREFIX + GdcCallContext.getCurrentContext().getProjectId());
        addLink(PROCESS, process);
    }

    /**
     *
     * @return project uri
     */
    @JsonIgnore
    public String getProject() {
        return getLink(PROJECT);
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
