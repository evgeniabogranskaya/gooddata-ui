/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * ETL process execution audit log event
 */
public class ETLProcessExecutionAuditLogEvent extends ETLProcessAuditLogEvent {

    private static final String EXECUTION = "execution";

    public ETLProcessExecutionAuditLogEvent(final String type,
                                            final String userLogin,
                                            final String userIp,
                                            final String domainId,
                                            final boolean success,
                                            final String project,
                                            final String process,
                                            final String execution) {
        super(type, userLogin, userIp, domainId, success, project, process);

        addLink(EXECUTION, execution);
    }

    /**
     * Create audit log event and fill some attributes from GdcCallContext
     *
     * @param type    event type
     * @param success was this event successful
     * @param process ETL process uri
     * @param execution ETL execution uri
     */
    public ETLProcessExecutionAuditLogEvent(final String type, final boolean success, final String process, final String execution) {
        super(type, success, process);
        
        addLink(EXECUTION, execution);
    }

    /**
     *
     * @return execution uri
     */
    @JsonIgnore
    public String getExecution() {
        return getLink(EXECUTION);
    }
}
