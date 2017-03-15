/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Common parent for Audit Log Services.
 * Enhances Audit Events with the component name.
 * Provides ability to disable logging using JMX or <code>gdc.audit-log.enabled</code> property.
 */
@ManagedResource
abstract class AbstractAuditLogService implements AuditLogService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${gdc.audit-log.enabled:true}")
    private boolean loggingEnabled = true;

    private final String component;

    AbstractAuditLogService(final String component) {
        this.component = notEmpty(component, "component");
    }

    @Override
    public void logEvent(final AuditLogEvent event) {
        if (!loggingEnabled) {
            return;
        }
        notNull(event, "event");
        event.setComponent(component);
        doLogEvent(event);
    }

    protected abstract void doLogEvent(final AuditLogEvent event);

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    @ManagedAttribute(description = "Enables/Disables audit logging for a component")
    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }
}
