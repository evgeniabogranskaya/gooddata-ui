/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Audit log service for testing - logs messages using SLF4J
 */
public class NoopAuditEventService extends AbstractAuditLogService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public NoopAuditEventService() {
        super("test");
    }

    @Override
    protected void logEvent(final String eventData) {
        logger.info(eventData);
    }
}
