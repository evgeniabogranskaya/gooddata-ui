/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@JsonTest
public class AuditEventDTOTest {

    @Autowired
    private JacksonTester<AuditEventDTO> json;

    private AuditEventDTO event = new AuditEventDTO("123", "default", "user123", new DateTime(1993, 9, 3, 0, 0, DateTimeZone.UTC));

    @Test
    public void testSerialize() throws Exception {
        json.write(event).assertThat().isEqualToJson("auditEvent.json");
    }

    @Test
    public void testDeserialize() throws Exception {
        String content = IOUtils.toString(getClass().getResourceAsStream("auditEvent.json"));

        json.parse(content).assertThat().isEqualTo(event);
    }

}