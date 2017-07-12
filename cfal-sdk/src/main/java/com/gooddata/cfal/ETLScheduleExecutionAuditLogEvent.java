/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * ETL schedule execution audit log event
 */
public class ETLScheduleExecutionAuditLogEvent extends ETLScheduleAuditLogEvent {

    private static final String EXECUTION = "execution";

    public ETLScheduleExecutionAuditLogEvent(final String type,
                                             final String userLogin,
                                             final String userIp,
                                             final String domainId,
                                             final boolean success,
                                             final String project,
                                             final String process,
                                             final String schedule,
                                             final String execution) {
        super(type, userLogin, userIp, domainId, success, project, process, schedule);

        addLink(EXECUTION, execution);
    }

    /**
     * Create audit log event and fill some attributes from GdcCallContext
     *
     * @param type     event type
     * @param success  was this event successful
     * @param process  ETL process uri
     * @param schedule ETL schedule uri
     * @param execution ETL execution uri
     */
    public ETLScheduleExecutionAuditLogEvent(final String type, final boolean success, final String process, final String schedule, final String execution) {
        super(type, success, process, schedule);
        addLink(EXECUTION, execution);
    }

    /**
     * @return ETL schedule uri
     */
    @JsonIgnore
    public String getExecution() {
        return getLink(EXECUTION);
    }
}
