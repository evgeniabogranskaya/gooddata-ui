/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

import static com.gooddata.cfal.restapi.util.DateUtils.date;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.HashMap;

@RunWith(SpringRunner.class)
@JsonTest
public class AuditEventDTOTest {

    @Autowired
    private JacksonTester<AuditEventDTO> json;

    private static String PARAM_KEY = "KEY";
    private static String PARAM_VALUE = "VALUE";

    private final AuditEventDTO event = new AuditEventDTO("123", "bear@gooddata.com", date("1993-03-09"), date("1993-03-09"), "127.0.0.1", true, "login", new HashMap<>());
    private final AuditEventDTO eventWithParams = new AuditEventDTO("123", "bear@gooddata.com", date("1993-03-09"), date("1993-03-09"), "127.0.0.1", true, "login",
            new HashMap<String, String>() {{
                put(PARAM_KEY, PARAM_VALUE);
            }});


    @Test
    public void testSerialize() throws Exception {
        json.write(event).assertThat().isEqualToJson("auditEvent.json", JSONCompareMode.STRICT);
    }

    @Test
    public void testDeserialize() throws Exception {
        String content = IOUtils.toString(getClass().getResourceAsStream("auditEvent.json"));

        final AuditEventDTO deserializedObject = json.parse(content).getObject();
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
        json.write(eventWithParams).assertThat().isEqualToJson("auditEventWithParam.json", JSONCompareMode.STRICT);
    }

    @Test
    public void testDeserializeWithParams() throws Exception {
        String content = IOUtils.toString(getClass().getResourceAsStream("auditEventWithParam.json"));

        final AuditEventDTO deserializedObject = json.parse(content).getObject();
        assertThat(deserializedObject.getParams(), is(not(Collections.emptyMap())));
    }

}