/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import static org.apache.commons.lang3.Validate.notEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * ETL schedule event
 */
public class ETLScheduleAuditLogEvent extends ETLProcessAuditLogEvent {

    private static final String SCHEDULE_ID = "scheduleId";

    public ETLScheduleAuditLogEvent(final AuditLogEventType type,
                                    final String userLogin,
                                    final String userIp,
                                    final String domainId,
                                    final boolean success,
                                    final String projectId,
                                    final String processId,
                                    final String scheduleId) {
        super(type, userLogin, userIp, domainId, success, projectId, processId);

        notEmpty(scheduleId, "scheduleId");

        addParam(SCHEDULE_ID, scheduleId);
    }

    /**
     * Create audit log event and fill some attributes from GdcCallContext
     * @param type event type
     * @param success was this event successful
     * @param processId ETL process ID
     * @param scheduleId ETL schedule ID
     */
    public ETLScheduleAuditLogEvent(final AuditLogEventType type, final boolean success, final String processId, final String scheduleId) {
        super(type, success, processId);
        addParam(SCHEDULE_ID, scheduleId);
    }

    @JsonIgnore
    public String getScheduleId() {
        return getParam(SCHEDULE_ID);
    }
}
