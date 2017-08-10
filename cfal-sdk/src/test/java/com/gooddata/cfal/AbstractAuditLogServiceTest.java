/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static com.gooddata.cfal.CfalMonitoringMetricConstants.LOG_CALL_COUNT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;

import java.util.Map;

public class AbstractAuditLogServiceTest {

    private static final String COMPONENT = "component";
    private AbstractAuditLogService instance;
    private AbstractAuditLogService spyInstance;
    private AuditLogEvent auditEvent;

    @Before
    public void setUp() throws Exception {
        instance = new AbstractAuditLogService(COMPONENT) {
            @Override
            protected void doLogEvent(AuditLogEvent event) {
            }
        };

        spyInstance = Mockito.spy(instance);
        auditEvent = new AuditLogEvent("FOO", "login", "userIp", "domain", true);
    }

    @Test(expected = NullPointerException.class)
    public void nullContructor() throws Exception {
        new AbstractAuditLogService(null) {
            @Override
            protected void doLogEvent(AuditLogEvent event) {
            }

        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyContructor() throws Exception {
        new AbstractAuditLogService("") {
            @Override
            protected void doLogEvent(AuditLogEvent event) {
            }
        };
    }

    @Test
    public void componentIsSet() throws Exception {
        instance.logEvent(auditEvent);
        assertThat(auditEvent.getComponent(), is(COMPONENT));
    }

    @Test
    public void logMethodIsCalled() throws Exception {
        spyInstance.logEvent(auditEvent);
        Mockito.verify(spyInstance).logEvent(any());
    }

    @Test
    public void logMethodIsNotCalledWhenLoggingIsDisabled() throws Exception {
        spyInstance.setLoggingEnabled(false);
        spyInstance.logEvent(auditEvent);
        Mockito.verify(spyInstance, never()).doLogEvent(any());
    }

    @Test
    public void testSetLoggingEnabled() throws Exception {
        instance.setLoggingEnabled(true);
        assertThat(instance.isLoggingEnabled(), is(true));
        instance.setLoggingEnabled(false);
        assertThat(instance.isLoggingEnabled(), is(false));
    }

    @Test
    public void doNoLogInvalidEvent() {
        spyInstance.logEvent(new AuditLogEvent("FOO", null, null, null));
        Mockito.verify(spyInstance, never()).doLogEvent(any());
    }

    @Test
    public void testGetMetrics() throws Exception {
        final Map<String, Metric> metrics = instance.getMetrics();

        assertThat(metrics, hasEntry(equalTo(LOG_CALL_COUNT), instanceOf(Gauge.class)));
    }

    @Test
    public void getLogEventCount() throws Exception {
        instance.logEvent(auditEvent);
        instance.logEvent(auditEvent);
        instance.logEvent(auditEvent);

        assertThat(instance.getLogEventCount(), is(3L));
    }
}