/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.gooddata.cfal.RemoteAuditLogFile.asString;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.core.util.ResourceUtils.resource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.joda.time.DateTimeZone.UTC;

public class RemoteAuditLogFileTest {

    @BeforeMethod
    public void setUp() throws Exception {
        DateTimeUtils.setCurrentMillisFixed(new DateTime(2017, 8, 3, 18, 56, 4, 470, UTC).getMillis());
    }

    @AfterMethod
    public void tearDown() throws Exception {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void shouldSerializeDomainWithDollar() throws Exception {
        final AuditLogEvent event = new AuditLogEvent("foo", "bear@gooddata.com", "127.0.0.1", "$domain");
        final String json = asString(event);
        assertThat(json, jsonEquals(resource("cfal/event-dollar-domain.json")));
    }

    @Test
    public void shouldSerializeNullDomain() throws Exception {
        final AuditLogEvent event = new AuditLogEvent("foo", "bear@gooddata.com", "127.0.0.1", null);
        final String json = asString(event);
        assertThat(json, jsonEquals(resource("cfal/event-null-domain.json")));
    }

    @Test
    public void shouldSerializeExtraProperties() throws Exception {
        final AuditLogEvent event = new ExtensibleAuditLogEvent("foo", "bear@gooddata.com", "127.0.0.1", "test")
                .withProperty("$fo.o", "bar");
        final String json = asString(event);
        assertThat(json, jsonEquals(resource("cfal/event-with-extra.json")));
    }
}