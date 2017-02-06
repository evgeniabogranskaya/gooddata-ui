/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import com.gooddata.collections.Paging;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static com.gooddata.cfal.restapi.dto.AuditEventDTO.ADMIN_URI;
import static com.gooddata.cfal.restapi.util.DateUtils.date;

@RunWith(SpringRunner.class)
@JsonTest
public class AuditEventsDTOTest {

    @Autowired
    private JacksonTester<AuditEventsDTO> json;

    private final AuditEventsDTO events = new AuditEventsDTO(
            Arrays.asList(new AuditEventDTO("123", "default", "user123", date("1993-03-09"), date("1993-03-09")),
                    new AuditEventDTO("456", "default", "user456", date("1993-03-09"), date("1993-03-09"))),
            new Paging("/gdc/audit/admin/events?offset=456&limit=100"),
            new HashMap<String, String>() {{
                put("self", ADMIN_URI);
            }});

    private final AuditEventsDTO emptyEvents = new AuditEventsDTO(
            Collections.emptyList(),
            new Paging(null),
            new HashMap<String, String>() {{
                put("self", ADMIN_URI);
            }});

    @Test
    public void testSerialize() throws Exception {
        json.write(events).assertThat().isEqualToJson("auditEvents.json");
    }

    @Test
    public void testDeserialize() throws Exception {
        String content = IOUtils.toString(getClass().getResourceAsStream("auditEvents.json"));

        json.parse(content).assertThat().isEqualTo(events);
    }

    @Test
    public void testSerializeEmptyEvents() throws Exception{
        json.write(emptyEvents).assertThat().isEqualToJson("emptyAuditEvents.json");
    }

    @Test
    public void testDeserializeEmptyEvents() throws Exception {
        String content = IOUtils.toString(getClass().getResourceAsStream("emptyAuditEvents.json"));
        json.parse(content).assertThat().isEqualTo(emptyEvents);
    }
}