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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(SpringRunner.class)
@JsonTest
public class AuditEventsDTOTest {

    private static final AuditEventDTO EVENT_1 = new AuditEventDTO("123", "default", "user123", date("1993-03-09"), date("1993-03-09"));
    private static final AuditEventDTO EVENT_2 = new AuditEventDTO("456", "default", "user456", date("1993-03-09"), date("1993-03-09"));
    private static final String NEXT_URI = "/gdc/audit/admin/events?offset=456&limit=100";
    @Autowired
    private JacksonTester<AuditEventsDTO> json;

    private static final AuditEventsDTO EVENTS = new AuditEventsDTO(
            Arrays.asList(EVENT_1, EVENT_2),
            new Paging(NEXT_URI),
            new HashMap<String, String>() {{
                put("self", ADMIN_URI);
            }});

    private static final AuditEventsDTO EMPTY_EVENTS = new AuditEventsDTO(
            Collections.emptyList(),
            new Paging(null),
            new HashMap<String, String>() {{
                put("self", ADMIN_URI);
            }});

    @Test
    public void testSerialize() throws Exception {
        json.write(EVENTS).assertThat().isEqualToJson("auditEvents.json");
    }

    @Test
    public void testDeserialize() throws Exception {
        String content = IOUtils.toString(getClass().getResourceAsStream("auditEvents.json"));

        final AuditEventsDTO deserialized = json.parse(content).getObject();
        assertThat(deserialized.getPaging().getNextUri(), is(NEXT_URI));
        assertThat(deserialized, hasSize(2));
        assertThat(deserialized.get(0).getId(), is(EVENT_1.getId()));
        assertThat(deserialized.get(1).getId(), is(EVENT_2.getId()));
    }

    @Test
    public void testSerializeEmptyEvents() throws Exception {
        json.write(EMPTY_EVENTS).assertThat().isEqualToJson("emptyAuditEvents.json");
    }

    @Test
    public void testDeserializeEmptyEvents() throws Exception {
        String content = IOUtils.toString(getClass().getResourceAsStream("emptyAuditEvents.json"));
        final AuditEventsDTO deserialized = json.parse(content).getObject();
        assertThat(deserialized.getPaging().getNextUri(), nullValue());
        assertThat(deserialized, hasSize(0));
    }
}