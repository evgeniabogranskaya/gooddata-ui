/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * ETL schedule event
 */
public class ETLScheduleAuditLogEvent extends ETLProcessAuditLogEvent {

    private static final String SCHEDULE = "schedule";

    public ETLScheduleAuditLogEvent(final AuditLogEventType type,
                                    final String userLogin,
                                    final String userIp,
                                    final String domainId,
                                    final boolean success,
                                    final String project,
                                    final String process,
                                    final String schedule) {
        super(type, userLogin, userIp, domainId, success, project, process);

        addLink(SCHEDULE, schedule);
    }

    /**
     * Create audit log event and fill some attributes from GdcCallContext
     * @param type event type
     * @param success was this event successful
     * @param process ETL process uri
     * @param schedule ETL schedule uri
     */
    public ETLScheduleAuditLogEvent(final AuditLogEventType type, final boolean success, final String process, final String schedule) {
        super(type, success, process);
        addLink(SCHEDULE, schedule);
    }

    /**
     *
     * @return ETL schedule uri
     */
    @JsonIgnore
    public String getSchedule() {
        return getLink(SCHEDULE);
    }
}
