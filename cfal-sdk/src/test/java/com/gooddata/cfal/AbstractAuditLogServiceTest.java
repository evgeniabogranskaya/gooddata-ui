/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;

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

            @Override
            public long getErrorCount() {
                return 0;
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

            @Override
            public long getErrorCount() {
                return 0;
            }
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyContructor() throws Exception {
        new AbstractAuditLogService("") {
            @Override
            protected void doLogEvent(AuditLogEvent event) {
            }

            @Override
            public long getErrorCount() {
                return 0;
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
}