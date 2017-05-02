/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.junit.Test;

import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;
import static net.javacrumbs.jsonunit.core.util.ResourceUtils.resource;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.HashMap;

public class AuditEventDTOTest {

    private final ObjectMapper json = new ObjectMapper();

    private static String PARAM_KEY = "KEY";
    private static String PARAM_VALUE = "VALUE";

    private static final DateTime DATE = new LocalDate(1993, 3, 9).toDateTimeAtStartOfDay(DateTimeZone.UTC);

    private final AuditEventDTO event = new AuditEventDTO("123", "bear@gooddata.com", DATE, DATE, "127.0.0.1", true, "login", new HashMap<>());
    private final AuditEventDTO eventWithParams = new AuditEventDTO("123", "bear@gooddata.com", DATE, DATE, "127.0.0.1", true, "login",
            new HashMap<String, String>() {{
                put(PARAM_KEY, PARAM_VALUE);
            }});


    @Test
    public void testSerialize() throws Exception {
        assertThat(event, jsonEquals(resource("com/gooddata/cfal/restapi/dto/auditEvent.json")));
    }

    @Test
    public void testDeserialize() throws Exception {
        String content = IOUtils.toString(resource("com/gooddata/cfal/restapi/dto/auditEvent.json"));

        final AuditEventDTO deserializedObject = json.readValue(content, AuditEventDTO.class);
        assertThat(deserializedObject, notNullValue());
        assertThat(deserializedObject.getId(), is(event.getId()));
        assertThat(deserializedObject.getOccurred(), is(event.getOccurred()));
        assertThat(deserializedObject.getRecorded(), is(event.getRecorded()));
        assertThat(deserializedObject.getUserLogin(), is(event.getUserLogin()));
        assertThat(deserializedObject.getUserIp(), is(event.getUserIp()));
        assertThat(deserializedObject.isSuccess(), is(event.isSuccess()));
        assertThat(deserializedObject.getType(), is(event.getType()));
    }

    @Test
    public void testSerializeEventWithParams() throws Exception {
        assertThat(eventWithParams, jsonEquals(resource("com/gooddata/cfal/restapi/dto/auditEventWithParam.json")));
    }

    @Test
    public void testDeserializeWithParams() throws Exception {
        String content = IOUtils.toString(resource("com/gooddata/cfal/restapi/dto/auditEventWithParam.json"));

        final AuditEventDTO deserializedObject = json.readValue(content, AuditEventDTO.class);
        assertThat(deserializedObject.getParams(), is(not(Collections.emptyMap())));
    }

}