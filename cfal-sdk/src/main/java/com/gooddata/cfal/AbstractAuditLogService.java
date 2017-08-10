/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import static com.gooddata.cfal.CfalMonitoringMetricConstants.LOG_CALL_COUNT;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Common parent for Audit Log Services.
 * Enhances Audit Events with the component name.
 * Provides ability to disable logging using JMX or <code>gdc.cfal.enabled</code> property.
 */
@ManagedResource
public abstract class AbstractAuditLogService implements AuditLogService, MetricSet {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${gdc.cfal.enabled:false}")    // disable cfal by default in components using this library from spring
    private boolean loggingEnabled = true; // enable cfal for direct usage e.g. in unit tests

    private final String component;

    private long logEventCount = 0;

    AbstractAuditLogService(final String component) {
        this.component = notEmpty(component, "component");
    }

    @Override
    public void logEvent(final AuditLogEvent event) {
        logEventCount++;
        if (!loggingEnabled) {
            logger.info("action=cfal status=disabled event=" + event);
            return;
        }
        if (!event.isValid()) {
            logger.warn("action=cfal status=invalid event=" + event);
            return;
        }
        notNull(event, "event");
        event.setComponent(component);
        doLogEvent(event);
    }

    public long getLogEventCount() {
        return logEventCount;
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final HashMap<String, Metric> gauges = new HashMap<>();
        final Gauge<Long> gaugeLogEventCount = () -> getLogEventCount();

        gauges.put(LOG_CALL_COUNT, gaugeLogEventCount);

        return gauges;
    }

    protected abstract void doLogEvent(final AuditLogEvent event);

    @ManagedAttribute(description = "Enables/Disables audit logging for a component")
    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    @ManagedAttribute(description = "Enables/Disables audit logging for a component")
    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    public String getComponent() {
        return component;
    }
}
