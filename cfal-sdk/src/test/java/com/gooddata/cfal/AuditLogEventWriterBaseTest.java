/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static com.gooddata.cfal.AuditLogEventWriterBase.format;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.core.util.ResourceUtils.resource;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class AuditLogEventWriterBaseTest {

    private AuditLogEvent event;

    @Before
    public void setUp() throws Exception {
        DateTimeUtils.setCurrentMillisFixed(new DateTime(2017, 3, 10, 9, 47, 3, 547, DateTimeZone.UTC).getMillis());
        event = new AuditLogEvent("FOO", "user@example.com", "1.2.3.4", "default");
        event.setComponent("foo");
    }

    @After
    public void tearDown() throws Exception {
        DateTimeUtils.currentTimeMillis();
    }

    @Test
    public void shouldSerialize() throws Exception {
        final String json = format(event);
        assertThat(json, jsonEquals(resource("foo.json")));
        assertThat(json, endsWith("\n"));
        assertThat(json, not(endsWith("\n\n")));
    }

    @Test
    public void shouldWriteEvent() throws Exception {
        final StringWriter sw = new StringWriter();
        final AuditLogEventWriterBase writer = new AuditLogEventWriterBase(sw);
        writer.logEvent(event);

        assertThat(sw.toString(), jsonEquals(resource("foo.json")));
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailOnNullEvent() throws Exception {
        final AuditLogEventWriterBase writer = new AuditLogEventWriterBase(new StringWriter());
        writer.logEvent(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailOnNullComponent() throws Exception {
        final AuditLogEventWriterBase writer = new AuditLogEventWriterBase(new StringWriter());
        event.setComponent(null);
        writer.logEvent(event);
    }

    @Test
    public void shouldSerializeLinksField() throws Exception {
        final ProjectAuditLogEvent event = new ProjectAuditLogEvent("type", "user@example.com", "1.2.3.4", "default",
                "/gdc/projects/project", true);
        event.setComponent("foo");

        final String json = format(event);
        assertThat(json, jsonEquals(resource("projectEvent.json")));
    }

    @Test
    public void shouldFailOnWrite() throws Exception {
        final Writer badWriter = mock(Writer.class);

        doThrow(IOException.class).when(badWriter).flush();

        final AuditLogEventWriterBase auditLogEventWriterBase = new AuditLogEventWriterBase(badWriter);
        final int written = auditLogEventWriterBase.logEvent(event);

        assertThat(written, is(0));
        assertThat(auditLogEventWriterBase.getErrorCount(), is(1L));
    }

}