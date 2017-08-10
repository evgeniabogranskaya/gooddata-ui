/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

public class Slf4jAuditLogEventWriterTest {
    private AuditLogEvent event;

    @Before
    public void setUp() {
        event = new AuditLogEvent("FOO", "user@example.com", "1.2.3.4", "default");
        event.setComponent("foo");
    }

    @Test
    public void logEvent() throws Exception {
        final String s = new ObjectMapper().writeValueAsString(event) + "\n";
        final int written = new Slf4jAuditLogEventWriter().logEvent(event);

        assertThat(s.length(), is(written));
    }

}