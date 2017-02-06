/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.rest;

import com.gooddata.cfal.restapi.config.WebConfig;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.restapi.dto.AuditEventsDTO;
import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.cfal.restapi.exception.InvalidOffsetException;
import com.gooddata.cfal.restapi.exception.InvalidTimeIntervalException;
import com.gooddata.cfal.restapi.exception.OffsetAndFromSpecifiedException;
import com.gooddata.cfal.restapi.exception.UserNotDomainAdminException;
import com.gooddata.cfal.restapi.exception.UserNotSpecifiedException;
import com.gooddata.cfal.restapi.service.AuditEventService;
import com.gooddata.cfal.restapi.service.UserDomainService;
import com.gooddata.collections.Paging;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;

import static com.gooddata.cfal.restapi.util.DateUtils.date;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(AuditEventController.class)
@Import(WebConfig.class)
public class AuditEventControllerTest {

    private static final String X_PUBLIC_USER_ID = "X-GDC-PUBLIC-USER-ID";

    private static final String USER_ID = "TEST_ID";

    private static final String NOT_ADMIN_USER_ID = "NOT_ADMIN";

    private static final String BAD_OFFSET = "badOffset";

    private static final String DOMAIN = "test domain";

    private static final ObjectId OFFSET = new ObjectId();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditEventService auditEventService;

    @MockBean
    private UserDomainService userDomainService;

    private static final DateTime LOWER_BOUND = date("1990-01-01");
    private static final DateTime UPPER_BOUND = date("2005-01-01");

    private final AuditEventsDTO domainEvents = new AuditEventsDTO(
            Arrays.asList(new AuditEventDTO("123", "default", "user123", date("1993-03-09"), date("1993-03-09")),
                    new AuditEventDTO("456", "default", "user456", date("1993-03-09"), date("1993-03-09"))),
            new Paging("/gdc/audit/admin/events?offset=456&limit=" + RequestParameters.DEFAULT_LIMIT),
            new HashMap<String, String>() {{
                put("self", AuditEventDTO.ADMIN_URI);
            }});

    private final AuditEventsDTO eventsForUser = new AuditEventsDTO(
            Arrays.asList(new AuditEventDTO("123", "default", "user123", date("1993-03-09"), date("1993-03-09")),
                    new AuditEventDTO("456", "default", "user123", date("1993-03-09"), date("1993-03-09"))),
            new Paging("/gdc/audit/admin/events?offset=456&limit=" + RequestParameters.DEFAULT_LIMIT),
            new HashMap<String, String>() {{
                put("self", AuditEventDTO.USER_URI);
            }});

    private final AuditEventsDTO domainEventsWithTimeInterval = new AuditEventsDTO(
            Arrays.asList(new AuditEventDTO("123", "default", "user123", date("1993-03-09"), date("1993-03-09")),
                    new AuditEventDTO("456", "default", "user456", date("1995-03-09"), date("1995-03-09"))),
            new Paging("/gdc/audit/admin/events?to=" + UPPER_BOUND + "&offset=456&limit=100"),
            new HashMap<String, String>() {{
                put("self", AuditEventDTO.ADMIN_URI);
            }});

    @Before
    public void setUp() {
        doReturn(DOMAIN).when(userDomainService).findDomainForUser(USER_ID);
        doReturn(DOMAIN).when(userDomainService).findDomainForUser(NOT_ADMIN_USER_ID);
        doThrow(new UserNotDomainAdminException("")).when(userDomainService).authorizeAdmin(NOT_ADMIN_USER_ID, DOMAIN);

        RequestParameters pageRequestWithBadOffset = new RequestParameters();
        pageRequestWithBadOffset.setOffset(BAD_OFFSET);

        RequestParameters pageRequestDefault = new RequestParameters();

        when(auditEventService.findByDomain(eq(DOMAIN), eq(pageRequestWithBadOffset))).thenThrow(new InvalidOffsetException(""));
        when(auditEventService.findByDomain(eq(DOMAIN), eq(pageRequestDefault))).thenReturn(domainEvents);

        when(auditEventService.findByDomainAndUser(eq(DOMAIN), eq(USER_ID), eq(pageRequestWithBadOffset))).thenThrow(new InvalidOffsetException(""));
        when(auditEventService.findByDomainAndUser(eq(DOMAIN), eq(USER_ID), eq(pageRequestDefault))).thenReturn(eventsForUser);

        RequestParameters boundedRequestParameters = new RequestParameters();
        boundedRequestParameters.setFrom(LOWER_BOUND);
        boundedRequestParameters.setTo(UPPER_BOUND);
        when(auditEventService.findByDomain(eq(DOMAIN), eq(boundedRequestParameters))).thenReturn(domainEventsWithTimeInterval);

        RequestParameters lowerBoundRequestParameters = new RequestParameters();
        lowerBoundRequestParameters.setFrom(LOWER_BOUND);

        RequestParameters pageRequestWithOffset = new RequestParameters();
        pageRequestWithOffset.setOffset(OFFSET.toString());
        pageRequestWithOffset.setFrom(LOWER_BOUND);

        when(auditEventService.findByDomain(eq(DOMAIN), eq(pageRequestWithOffset))).thenThrow(new OffsetAndFromSpecifiedException(""));
        when(auditEventService.findByDomainAndUser(eq(DOMAIN), eq(USER_ID), eq(pageRequestWithOffset))).thenThrow(new OffsetAndFromSpecifiedException(""));

    }

    @Test
    public void testListAuditEventsUserNotSpecified() throws Exception {
        mockMvc.perform(get(AuditEventDTO.ADMIN_URI))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error.errorClass", is(UserNotSpecifiedException.class.getName())));
    }

    @Test
    public void testListAuditEventsInvalidOffset() throws Exception {
        mockMvc.perform(get(AuditEventDTO.ADMIN_URI)
                .param("offset", BAD_OFFSET)
                .header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(InvalidOffsetException.class.getName())));
    }

    @Test
    public void testListAuditEventsNotAdmin() throws Exception {
        mockMvc.perform(get(AuditEventDTO.ADMIN_URI)
                .header(X_PUBLIC_USER_ID, NOT_ADMIN_USER_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.errorClass", is(UserNotDomainAdminException.class.getName())));
    }

    @Test
    public void testListAuditEventsDefaultPaging() throws Exception {
        mockMvc.perform(get(AuditEventDTO.ADMIN_URI)
                .header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(IOUtils.toString(getClass().getResourceAsStream("auditEvents.json"))));
    }

    @Test
    public void testListAuditEventsForUserNotSpecified() throws Exception {
        mockMvc.perform(get(AuditEventDTO.USER_URI))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(UserNotSpecifiedException.class.getName())));
    }

    @Test
    public void testListAuditEventsForUserInvalidUser() throws Exception {
        mockMvc.perform(get(AuditEventDTO.USER_URI)
                .param("offset", BAD_OFFSET)
                .header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(InvalidOffsetException.class.getName())));
    }

    @Test
    public void testListAuditEventsForUserDefaultPaging() throws Exception {
        mockMvc.perform(get(AuditEventDTO.USER_URI)
                .header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(IOUtils.toString(getClass().getResourceAsStream("userAuditEvents.json"))));
    }

    @Test
    public void testListAuditEventsWithTimeInterval() throws Exception {
        mockMvc.perform(get(AuditEventDTO.ADMIN_URI)
                .param("from", LOWER_BOUND.toString())
                .param("to", UPPER_BOUND.toString())
                .header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(IOUtils.toString(getClass().getResourceAsStream("auditEventsWithTimeInterval.json"))));
    }

    @Test
    public void testListAuditEventsWithBadLimit() throws Exception {
        mockMvc.perform(get(AuditEventDTO.ADMIN_URI)
                .param("limit", "not number")
                .header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testListAuditEventsForUserWithBadLimit() throws Exception {
        mockMvc.perform(get(AuditEventDTO.USER_URI)
                .param("limit", "not number")
                .header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testListAuditEventsInvalidFrom() throws Exception {
        mockMvc.perform(get(AuditEventDTO.ADMIN_URI)
                .param("from", "a")
                .param("to", UPPER_BOUND.toString())
                .header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testListAuditEventsInvalidTo() throws Exception {
        mockMvc.perform(get(AuditEventDTO.ADMIN_URI)
                .param("from", LOWER_BOUND.toString())
                .param("to", "a")
                .header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testListAuditEventsForUserInvalidFrom() throws Exception {
        mockMvc.perform(get(AuditEventDTO.USER_URI)
                .param("from", "a")
                .param("to", UPPER_BOUND.toString())
                .header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testListAuditEventsForUserInvalidTo() throws Exception {
        mockMvc.perform(get(AuditEventDTO.USER_URI)
                .param("from", LOWER_BOUND.toString())
                .param("to", "a")
                .header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testListAuditEventsExpectedContentType() throws Exception {
        mockMvc.perform(get(AuditEventDTO.ADMIN_URI)
                .header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", APPLICATION_JSON_VALUE + ";charset=UTF-8"));
    }

    @Test
    public void testListAuditEventsForUserExpectedContentType() throws Exception {
        mockMvc.perform(get(AuditEventDTO.USER_URI)
                .header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", APPLICATION_JSON_VALUE + ";charset=UTF-8"));
    }

    @Test
    public void testListAuditEventsFromAndOffsetSpecified() throws Exception {
        mockMvc.perform(get(AuditEventDTO.ADMIN_URI)
                .param("offset", OFFSET.toString())
                .param("from", LOWER_BOUND.toString())
                .header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(OffsetAndFromSpecifiedException.class.getName())));
    }

    @Test
    public void testListAuditEventsForUserFromAndOffsetSpecified() throws Exception {
        mockMvc.perform(get(AuditEventDTO.USER_URI)
                .param("offset", OFFSET.toString())
                .param("from", LOWER_BOUND.toString())
                .header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(OffsetAndFromSpecifiedException.class.getName())));
    }

    @Test
    public void testListAuditEventsForUserInvalidTimeInterval() throws Exception {
        mockMvc.perform(get(AuditEventDTO.USER_URI)
                .param("from", UPPER_BOUND.toString())
                .param("to", LOWER_BOUND.toString())
                .header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(InvalidTimeIntervalException.class.getName())));
    }

    @Test
    public void testListAuditEventsInvalidTimeInterval() throws Exception {
        mockMvc.perform(get(AuditEventDTO.ADMIN_URI)
                .param("from", UPPER_BOUND.toString())
                .param("to", LOWER_BOUND.toString())
                .header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(InvalidTimeIntervalException.class.getName())));
    }
}
