/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SimpleAuditLogServiceTest {

    private static final AuditLogEvent EVENT = new AuditLogEvent(AuditLogEventType.STANDARD_LOGIN, "bear@gooddata.com", "1.2.3.4", "default");

    private SimpleAuditLogService service;
    private AuditLogEventFileWriter writer;

    @Before
    public void setUp() throws Exception {
        writer = mock(AuditLogEventFileWriter.class);
        service = new SimpleAuditLogService("foo", writer);
    }

    @Test
    public void shouldLogEvent() throws Exception {
        service.logEvent(EVENT);
        verify(writer).logEvent(EVENT);
    }

    @Test
    public void shouldCloseWriter() throws Exception {
        service.destroy();
        verify(writer).close();
    }
}