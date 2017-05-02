/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import static org.apache.commons.lang3.Validate.notEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gooddata.context.GdcCallContext;

/**
 * ETL process AuditLogEvent
 */
public class ETLProcessAuditLogEvent extends AuditLogEvent {

    private static final String PROJECT_ID = "projectId";

    private static final String PROCESS_ID = "processId";

    public ETLProcessAuditLogEvent(final AuditLogEventType type,
                                   final String userLogin,
                                   final String userIp,
                                   final String domainId,
                                   final boolean success,
                                   final String projectId,
                                   final String processId) {
        super(type, userLogin, userIp, domainId, success);

        notEmpty(projectId, "projectId");
        notEmpty(processId, "processId");

        addParam(PROJECT_ID, projectId);
        addParam(PROCESS_ID, processId);
    }

    /**
     * Create audit log event and fill some attributes from GdcCallContext
     *
     * @param type      event type
     * @param success   was this event successful
     * @param processId ETL process ID
     */
    public ETLProcessAuditLogEvent(final AuditLogEventType type, final boolean success, final String processId) {
        super(type, success);
        addParam(PROJECT_ID, GdcCallContext.getCurrentContext().getProjectId());
        addParam(PROCESS_ID, processId);
    }

    @JsonIgnore
    public String getProjectId() {
        return getParam(PROJECT_ID);
    }

    @JsonIgnore
    public String getProcessId() {
        return getParam(PROCESS_ID);
    }
}
