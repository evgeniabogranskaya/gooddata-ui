/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

import static com.gooddata.cfal.restapi.util.DateUtils.date;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@JsonTest
public class AuditEventDTOTest {

    @Autowired
    private JacksonTester<AuditEventDTO> json;

    private final AuditEventDTO event = new AuditEventDTO("123", "bear@gooddata.com", date("1993-03-09"), date("1993-03-09"), "127.0.0.1", true, "login");

    @Test
    public void testSerialize() throws Exception {
        json.write(event).assertThat().isEqualToJson("auditEvent.json");
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

}