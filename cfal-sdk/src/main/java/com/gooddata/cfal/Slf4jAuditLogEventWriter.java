/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import static com.gooddata.cfal.AuditLogEventWriterBase.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Formats Audit Events as one-line JSON and writes them into slf4j logger
 */
public class Slf4jAuditLogEventWriter implements AuditLogEventWriter {

    private final Logger logger = LoggerFactory.getLogger(Slf4jAuditLogEventWriter.class);

    @Override
    public int logEvent(final AuditLogEvent event) {
        try {
            final String eventData = format(event);
            logger.info("component={} type={} userLogin={} userIp={} domainId={} success={}", event.getComponent(),
                    event.getType(), event.getUserLogin(), event.getUserIp(), event.getDomainId(), event.isSuccess());
            logger.debug(eventData);
            return eventData.length();
        } catch (JsonProcessingException e) {
            logger.error("Unable to write event={}", event.getType(), e);
            return 0;
        }
    }
}
