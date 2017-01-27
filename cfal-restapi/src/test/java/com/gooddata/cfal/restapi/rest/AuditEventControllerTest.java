/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.rest;

import com.gooddata.cfal.restapi.config.WebConfig;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.restapi.dto.AuditEventsDTO;
import com.gooddata.cfal.restapi.exception.InvalidOffsetException;
import com.gooddata.cfal.restapi.exception.UserNotDomainAdminException;
import com.gooddata.cfal.restapi.exception.UserNotSpecifiedException;
import com.gooddata.cfal.restapi.service.AuditEventService;
import com.gooddata.cfal.restapi.service.UserDomainService;
import com.gooddata.collections.PageRequest;
import com.gooddata.collections.Paging;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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

import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditEventService auditEventService;

    @MockBean
    private UserDomainService userDomainService;

    private final AuditEventsDTO domainEvents = new AuditEventsDTO(
            Arrays.asList(new AuditEventDTO("123", "default", "user123", new DateTime(1993, 9, 3, 0, 0, DateTimeZone.UTC)),
                    new AuditEventDTO("456", "default", "user456", new DateTime(1993, 9, 3, 0, 0, DateTimeZone.UTC))),
            new Paging("/gdc/audit/admin/events?offset=456&limit=" + PageRequest.DEFAULT_LIMIT),
            new HashMap<String, String>() {{
                put("self", AuditEventDTO.ADMIN_URI);
            }});

    private final AuditEventsDTO eventsForUser = new AuditEventsDTO(
            Arrays.asList(new AuditEventDTO("123", "default", "user123", new DateTime(1993, 9, 3, 0, 0, DateTimeZone.UTC)),
                    new AuditEventDTO("456", "default", "user123", new DateTime(1993, 9, 3, 0, 0, DateTimeZone.UTC))),
            new Paging("/gdc/audit/admin/events?offset=456&limit=" + PageRequest.DEFAULT_LIMIT),
            new HashMap<String, String>() {{
                put("self", AuditEventDTO.USER_URI);
            }});

    @Before
    public void setUp() {
        doReturn(DOMAIN).when(userDomainService).findDomainForUser(USER_ID);
        doReturn(DOMAIN).when(userDomainService).findDomainForUser(NOT_ADMIN_USER_ID);
        doThrow(new UserNotDomainAdminException("")).when(userDomainService).authorizeAdmin(NOT_ADMIN_USER_ID, DOMAIN);

        PageRequest pageRequestWithBadOffset = new PageRequest(BAD_OFFSET, PageRequest.DEFAULT_LIMIT);
        PageRequest pageRequestDefault = new PageRequest();

        when(auditEventService.findByDomain(eq(DOMAIN), eq(pageRequestWithBadOffset))).thenThrow(new InvalidOffsetException(""));
        when(auditEventService.findByDomain(eq(DOMAIN), eq(pageRequestDefault))).thenReturn(domainEvents);

        when(auditEventService.findByDomainAndUser(eq(DOMAIN), eq(USER_ID), eq(pageRequestWithBadOffset))).thenThrow(new InvalidOffsetException(""));
        when(auditEventService.findByDomainAndUser(eq(DOMAIN), eq(USER_ID), eq(pageRequestDefault))).thenReturn(eventsForUser);
    }

    @Test
    public void testListAuditEventsUserNotSpecified() throws Exception {
        mockMvc.perform(get(AuditEventDTO.ADMIN_URI))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error.errorClass", is(UserNotSpecifiedException.class.getName())));

    }

    @Test
    public void testListAuditEventsInvalidOffset() throws Exception {
        mockMvc.perform(get(AuditEventDTO.ADMIN_URI + "?offset=" + BAD_OFFSET).header(X_PUBLIC_USER_ID, USER_ID))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.error.errorClass", is(InvalidOffsetException.class.getName())));
    }

    @Test
    public void testListAuditEventsNotAdmin() throws Exception {
        mockMvc.perform(get(AuditEventDTO.ADMIN_URI).header(X_PUBLIC_USER_ID, NOT_ADMIN_USER_ID))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.error.errorClass", is(UserNotDomainAdminException.class.getName())));

    }

    @Test
    public void testListAuditEventsDefaultPaging() throws Exception {
        mockMvc.perform(get(AuditEventDTO.ADMIN_URI).header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isOk()).andExpect(content().json(IOUtils.toString(getClass().getResourceAsStream("auditEvents.json"))));
    }

    @Test
    public void testListAuditEventsForUserNotSpecified() throws Exception {
        mockMvc.perform(get(AuditEventDTO.USER_URI))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(UserNotSpecifiedException.class.getName())));
    }

    @Test
    public void testListAuditEventsForUserInvalidUser() throws Exception {
        mockMvc.perform(get(AuditEventDTO.USER_URI + "?offset=" + BAD_OFFSET).header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(InvalidOffsetException.class.getName())));
    }

    @Test
    public void testListAuditEventsForUserDefaultPaging() throws Exception {
        mockMvc.perform(get(AuditEventDTO.USER_URI).header(X_PUBLIC_USER_ID, USER_ID))
                .andExpect(status().isOk()).andExpect(content().json(IOUtils.toString(getClass().getResourceAsStream("userAuditEvents.json"))));
    }
}
