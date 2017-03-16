/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gooddata.cfal.AuditLogEventWriterBase.format;

/**
 * Audit log service which logs messages using SLF4J. Suitable for testing only.
 */
public class NoopAuditEventService extends AbstractAuditLogService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public NoopAuditEventService() {
        super("test");
    }

    @Override
    protected void doLogEvent(final AuditLogEvent event) {
        try {
            final String eventData = format(event);
            logger.info(eventData);
        } catch (JsonProcessingException e) {
            logger.error("Unable to write event={}", event.getType(), e);
        }
    }
}
