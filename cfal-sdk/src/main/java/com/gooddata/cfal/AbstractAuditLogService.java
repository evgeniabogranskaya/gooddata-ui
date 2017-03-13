/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.Validate.notEmpty;

/**
 * Common parent handling event formatting and adding component name to the event
 */
abstract class AbstractAuditLogService implements AuditLogService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final String component;

    AbstractAuditLogService(final String component) {
        this.component = notEmpty(component, "component");
    }

    @Override
    public void logEvent(final AuditLogEvent event) {
        event.setComponent(component);
        final String json = JsonFormatter.format(event);
        logEvent(json);
    }

    protected abstract void logEvent(String eventData);
}
