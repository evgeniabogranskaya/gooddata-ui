/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.gooddata.cfal.AuditLogEventWriterBase.format;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Audit log service which logs messages using SLF4J. Suitable for testing only.
 */
public class NoopAuditEventService extends AbstractAuditLogService {

    private final Logger logger;

    public NoopAuditEventService() {
        this(LoggerFactory.getLogger(NoopAuditEventService.class));
    }

    NoopAuditEventService(final Logger logger) {
        super("test");
        this.logger = notNull(logger, "logger");
    }

    @Override
    protected void doLogEvent(final AuditLogEvent event) {
        try {
            final String eventData = format(event);
            logger.info("component={} type={} userLogin={} userIp={} domainId={} success={}", event.getComponent(),
                    event.getType(), event.getUserLogin(), event.getUserIp(), event.getDomainId(), event.isSuccess());
            logger.debug(eventData);
        } catch (JsonProcessingException e) {
            logger.error("Unable to write event={}", event.getType(), e);
        }
    }
}
