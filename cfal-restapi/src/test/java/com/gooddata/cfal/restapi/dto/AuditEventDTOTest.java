/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import static com.gooddata.cfal.restapi.util.DateUtils.date;

import org.apache.commons.io.IOUtils;
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

    private final AuditEventDTO event = new AuditEventDTO("123", "default", "user123", date("1993-03-09"), date("1993-03-09"));

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