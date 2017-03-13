/**
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.gooddata.cfal.AuditLogEventType.STANDARD_LOGIN;
import static com.gooddata.cfal.JsonFormatter.format;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.core.util.ResourceUtils.resource;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonFormatterTest {

    @Before
    public void setUp() throws Exception {
        DateTimeUtils.setCurrentMillisFixed(new DateTime(2017, 3, 10, 9, 47, 3, 547, DateTimeZone.UTC).getMillis());
    }

    @After
    public void tearDown() throws Exception {
        DateTimeUtils.currentTimeMillis();
    }

    @Test
    public void shouldSerialize() throws Exception {
        final AuditLogEvent event = new AuditLogEvent(STANDARD_LOGIN, "user@example.com", "1.2.3.4", "default");
        event.setComponent("foo");
        final String json = format(event);
        assertThat(json, jsonEquals(resource("login.json")));
        assertThat(json, endsWith("\n"));
        assertThat(json, not(endsWith("\n\n")));
    }

}