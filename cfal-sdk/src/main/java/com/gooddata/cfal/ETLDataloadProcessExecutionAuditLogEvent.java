/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * ETL Dataload process execution audit log event
 */
public class ETLDataloadProcessExecutionAuditLogEvent extends ETLProcessExecutionAuditLogEvent {

    private static final String INSTANCE = "instance";

    public ETLDataloadProcessExecutionAuditLogEvent(final AuditLogEventType type,
                                            final String userLogin,
                                            final String userIp,
                                            final String domainId,
                                            final boolean success,
                                            final String project,
                                            final String process,
                                            final String execution,
                                            final String instance) {
        super(type, userLogin, userIp, domainId, success, project, process, execution);

        addLink(INSTANCE, instance);
    }

    /**
     * Create audit log event and fill some attributes from GdcCallContext
     *
     * @param type    event type
     * @param success was this event successful
     * @param process ETL process uri
     * @param execution ETL execution uri
     * @param instance ADS instance uri
     */
    public ETLDataloadProcessExecutionAuditLogEvent(final AuditLogEventType type, final boolean success, final String process, final String execution, final String instance) {
        super(type, success, process, execution);

        addLink(INSTANCE, instance);
    }

    /**
     *
     * @return ADS instance uri
     */
    @JsonIgnore
    public String getInstance() {
        return getLink(INSTANCE);
    }
}
