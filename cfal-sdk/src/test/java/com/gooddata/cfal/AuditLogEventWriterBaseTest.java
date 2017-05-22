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

import java.io.StringWriter;

import static com.gooddata.cfal.AuditLogEventType.ETL_SCHEDULE_CHANGE;
import static com.gooddata.cfal.AuditLogEventWriterBase.format;
import static com.gooddata.cfal.AuditLogEventType.STANDARD_LOGIN;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.core.util.ResourceUtils.resource;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.*;

public class AuditLogEventWriterBaseTest {

    private AuditLogEvent event;

    @Before
    public void setUp() throws Exception {
        DateTimeUtils.setCurrentMillisFixed(new DateTime(2017, 3, 10, 9, 47, 3, 547, DateTimeZone.UTC).getMillis());
        event = new AuditLogEvent(STANDARD_LOGIN, "user@example.com", "1.2.3.4", "default");
        event.setComponent("foo");
    }

    @After
    public void tearDown() throws Exception {
        DateTimeUtils.currentTimeMillis();
    }

    @Test
    public void shouldSerialize() throws Exception {
        final String json = format(event);
        assertThat(json, jsonEquals(resource("login.json")));
        assertThat(json, endsWith("\n"));
        assertThat(json, not(endsWith("\n\n")));
    }

    @Test
    public void shouldWriteEvent() throws Exception {
        final StringWriter sw = new StringWriter();
        final AuditLogEventWriterBase writer = new AuditLogEventWriterBase(sw);
        writer.logEvent(event);

        assertThat(sw.toString(), jsonEquals(resource("login.json")));
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
        ETLScheduleAuditLogEvent event = new ETLScheduleAuditLogEvent(ETL_SCHEDULE_CHANGE, "user@example.com", "1.2.3.4", "default",
                true, "/gdc/projects/project", "/gdc/projects/project/dataload/process/process",
                "/gdc/projects/project/schedules/schedule");
        event.setComponent("foo");

        final String json = format(event);
        assertThat(json, jsonEquals(resource("scheduleChange.json")));
    }

}