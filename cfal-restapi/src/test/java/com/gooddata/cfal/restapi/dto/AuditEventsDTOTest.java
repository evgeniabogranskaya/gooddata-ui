/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import com.gooddata.collections.Paging;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashMap;

import static com.gooddata.cfal.restapi.dto.AuditEventDTO.ADMIN_URI;

@RunWith(SpringRunner.class)
@JsonTest
public class AuditEventsDTOTest {

    @Autowired
    private JacksonTester<AuditEventsDTO> json;

    private final AuditEventsDTO events = new AuditEventsDTO(
            Arrays.asList(new AuditEventDTO("123", "default", "user123", new DateTime(1993, 9, 3, 0, 0, DateTimeZone.UTC)),
                          new AuditEventDTO("456", "default", "user456", new DateTime(1993, 9, 3, 0, 0, DateTimeZone.UTC))),
            new Paging("/gdc/audit/admin/events?offset=456&limit=100"),
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
}