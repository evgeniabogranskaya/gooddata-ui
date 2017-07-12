/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class NoopAuditEventServiceTest {

    private Logger logger;
    private NoopAuditEventService service;

    @Before
    public void setUp() throws Exception {
        logger = mock(Logger.class);
        service = new NoopAuditEventService(logger);
    }

    @Test
    public void shouldLogEventOnInfoLevel() throws Exception {
        service.logEvent(new AuditLogEvent("FOO", "bear@gooddata.com", "1.2.3.4", "default", true));

        verify(logger).info("component={} type={} userLogin={} userIp={} domainId={} success={}",
                "test", "FOO", "bear@gooddata.com", "1.2.3.4", "default", true);
    }
}