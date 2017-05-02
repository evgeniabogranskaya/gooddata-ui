/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import com.gooddata.collections.Paging;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.gooddata.cfal.restapi.dto.AuditEventDTO.ADMIN_URI_TEMPLATE;
import static com.gooddata.cfal.restapi.dto.AuditEventDTO.USER_URI_TEMPLATE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@RunWith(SpringRunner.class)
@JsonTest
public class AuditEventsDTOTest {

    private static final String USER1_ID = "user123";

    private static final String USER1_LOGIN = "bear@gooddata.com";
    private static final String USER2_LOGIN = "jane@gooddata.com";

    private static final String DOMAIN = "default";

    private static final String IP = "127.0.0.1";
    private static final boolean SUCCESS = true;
    private static final String TYPE = "login";
    private static final Map<String, String> EMPTY_PARAMS = new HashMap<>();

    private static final DateTime DATE = new LocalDate(1993, 3, 9).toDateTimeAtStartOfDay(DateTimeZone.UTC);
    private static final AuditEventDTO EVENT_1 = new AuditEventDTO("123", USER1_LOGIN, DATE, DATE, IP, SUCCESS, TYPE, EMPTY_PARAMS);
    private static final AuditEventDTO EVENT_2 = new AuditEventDTO("456", USER2_LOGIN, DATE, DATE, IP, SUCCESS, TYPE, EMPTY_PARAMS);

    private static final String ADMIN_URI = ADMIN_URI_TEMPLATE.expand(DOMAIN).toString();
    private static final String USER_URI = USER_URI_TEMPLATE.expand(USER1_ID).toString();
    private static final String ADMIN_NEXT_URI = ADMIN_URI + "?offset=456&limit=2";
    private static final String USER_NEXT_URI = USER_URI + "?offset=456&limit=1";


    @Autowired
    private JacksonTester<AuditEventsDTO> json;

    private static final AuditEventsDTO EVENTS = new AuditEventsDTO(
            Arrays.asList(EVENT_1, EVENT_2),
            new Paging(ADMIN_NEXT_URI),
            new HashMap<String, String>() {{
                put("self", ADMIN_URI);
            }});

    private static final AuditEventsDTO EMPTY_EVENTS = new AuditEventsDTO(
            Collections.emptyList(),
            new Paging(null),
            new HashMap<String, String>() {{
                put("self", ADMIN_URI);
            }});

    private static final AuditEventsDTO USER_EVENTS = new AuditEventsDTO(
            Collections.singletonList(EVENT_1),
            new Paging(USER_NEXT_URI),
            new HashMap<String, String>() {{
                put("self", USER_URI);
            }}
    );

    @Test
    public void testSerialize() throws Exception {
        json.write(EVENTS).assertThat().isEqualToJson("auditEvents.json", JSONCompareMode.STRICT);
    }

    @Test
    public void testDeserialize() throws Exception {
        String content = IOUtils.toString(getClass().getResourceAsStream("auditEvents.json"));

        final AuditEventsDTO deserialized = json.parse(content).getObject();
        assertThat(deserialized.getPaging().getNextUri(), is(ADMIN_NEXT_URI));
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

    @Test
    public void testSerializeUserEvents() throws Exception {
        json.write(USER_EVENTS).assertThat().isEqualToJson("userAuditEvents.json");
    }

    @Test
    public void testDeserializeUserEvents() throws Exception {
        String content = IOUtils.toString(getClass().getResourceAsStream("userAuditEvents.json"));

        final AuditEventsDTO deserialized = json.parse(content).getObject();
        assertThat(deserialized.getPaging().getNextUri(), is(USER_NEXT_URI));
        assertThat(deserialized, hasSize(1));
        assertThat(deserialized.get(0).getId(), is(EVENT_1.getId()));
    }
}