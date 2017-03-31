/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import static org.apache.commons.lang3.Validate.notEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gooddata.context.GdcCallContext;

/**
 * ETL schedule event
 */
public class ETLScheduleAuditLogEvent extends AuditLogEvent {

    private static final String PROJECT_ID = "projectId";

    private static final String PROCESS_ID = "processId";

    private static final String SCHEDULE_ID = "scheduleId";

    public ETLScheduleAuditLogEvent(final AuditLogEventType type,
                                    final String userLogin,
                                    final String userIp,
                                    final String domainId,
                                    final boolean success,
                                    final String projectId,
                                    final String processId,
                                    final String scheduleId) {
        super(type, userLogin, userIp, domainId, success);

        notEmpty(projectId, "projectId");
        notEmpty(processId, "processId");
        notEmpty(scheduleId, "scheduleId");

        addParam(PROJECT_ID, projectId);
        addParam(PROCESS_ID, processId);
        addParam(SCHEDULE_ID, scheduleId);
    }

    @JsonIgnore
    public String getProjectId() {
        return getParam(PROJECT_ID);
    }

    @JsonIgnore
    public String getProcessId() {
        return getParam(PROCESS_ID);
    }

    @JsonIgnore
    public String getScheduleId() {
        return getParam(SCHEDULE_ID);
    }
}
