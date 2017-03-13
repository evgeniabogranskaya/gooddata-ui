/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;

public class AbstractAuditLogServiceTest {

    private static final String COMPONENT = "component";
    private AbstractAuditLogService instance;

    @Before
    public void setUp() throws Exception {
        instance = new AbstractAuditLogService(COMPONENT) {
            @Override
            protected void logEvent(String eventData) {
            }
        };
    }

    @Test(expected = NullPointerException.class)
    public void nullContructor() throws Exception {
        new AbstractAuditLogService(null) {
            @Override
            protected void logEvent(String eventData) {

            }
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyContructor() throws Exception {
        new AbstractAuditLogService("") {
            @Override
            protected void logEvent(String eventData) {

            }
        };
    }

    @Test
    public void componentIsSet() throws Exception {
        final AuditLogEvent event = new AuditLogEvent(AuditLogEventType.STANDARD_LOGIN, "login",
                "userIp", "domain", true);
        instance.logEvent(event);
        assertThat(event.getComponent(), is(COMPONENT));
    }

    @Test
    public void logMethodIsCalled() throws Exception {
        final AuditLogEvent event = new AuditLogEvent(AuditLogEventType.STANDARD_LOGIN, "login",
                "userIp", "domain", true);

        final AbstractAuditLogService instance = Mockito.spy(new AbstractAuditLogService(COMPONENT) {
            @Override
            protected void logEvent(String eventData) {
            }
        });

        instance.logEvent(event);
        Mockito.verify(instance).logEvent(anyString());
    }
}