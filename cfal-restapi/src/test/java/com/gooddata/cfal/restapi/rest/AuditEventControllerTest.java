/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.rest;

import com.gooddata.cfal.restapi.config.MonitoringTestConfig;
import com.gooddata.cfal.restapi.config.WebConfig;
import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.restapi.dto.AuditEventsDTO;
import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.cfal.restapi.dto.UserInfo;
import com.gooddata.cfal.restapi.exception.UserNotAuthorizedException;
import com.gooddata.cfal.restapi.exception.UserNotDomainAdminException;
import com.gooddata.cfal.restapi.exception.UserNotSpecifiedException;
import com.gooddata.cfal.restapi.exception.ValidationException;
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

import static com.gooddata.cfal.restapi.config.WebConfig.COMPONENT_NAME;
import static com.gooddata.cfal.restapi.dto.AuditEventDTO.ADMIN_URI_TEMPLATE;
import static com.gooddata.cfal.restapi.dto.AuditEventDTO.USER_URI_TEMPLATE;
import static com.gooddata.cfal.restapi.validation.RequestParametersValidator.INVALID_OFFSET_MSG;
import static com.gooddata.cfal.restapi.validation.RequestParametersValidator.INVALID_TIME_INTERVAL_MSG;
import static com.gooddata.cfal.restapi.validation.RequestParametersValidator.NOT_POSITIVE_LIMIT_MSG;
import static com.gooddata.cfal.restapi.validation.RequestParametersValidator.OFFSET_AND_FROM_SPECIFIED_MSG;
import static com.gooddata.cfal.restapi.util.DateUtils.date;
import static java.lang.String.format;
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
@Import({MonitoringTestConfig.class, WebConfig.class})
public class AuditEventControllerTest {

    private static final String X_PUBLIC_USER_ID = "X-GDC-PUBLIC-USER-ID";

    private static final String ADMIN_USER_ID = "ADMIN";

    private static final String NOT_ADMIN_USER_ID = "NOT_ADMIN";

    private static final String USER1_LOGIN = "bear@gooddata.com";

    private static final String USER2_LOGIN = "jane@gooddata.com";

    private static final String BAD_OFFSET = "badOffset";

    private static final String DOMAIN = "default";

    private static final ObjectId OFFSET = new ObjectId();

    private static final String TYPE_MISMATCH_MESSAGE = "Value \"%s\" is not valid for parameter \"%s\"";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditEventService auditEventService;

    @MockBean
    private UserDomainService userDomainService;

    private static final DateTime LOWER_BOUND = date("1990-01-01");
    private static final DateTime UPPER_BOUND = date("2005-01-01");

    private final AuditEventsDTO domainEvents = new AuditEventsDTO(
            Arrays.asList(new AuditEventDTO("123", USER1_LOGIN, date("1993-03-09"), date("1993-03-09")),
                    new AuditEventDTO("456", USER2_LOGIN, date("1993-03-09"), date("1993-03-09"))),
            new Paging(adminUri() + "?offset=456&limit=" + RequestParameters.DEFAULT_LIMIT),
            new HashMap<String, String>() {{
                put("self", adminUri());
            }});

    private final AuditEventsDTO eventsForAdminUser = new AuditEventsDTO(
            Arrays.asList(new AuditEventDTO("123", USER1_LOGIN, date("1993-03-09"), date("1993-03-09")),
                    new AuditEventDTO("456", USER1_LOGIN, date("1993-03-09"), date("1993-03-09"))),
            new Paging(userUri(ADMIN_USER_ID) + "?offset=456&limit=" + RequestParameters.DEFAULT_LIMIT),
            new HashMap<String, String>() {{
                put("self", userUri(ADMIN_USER_ID));
            }});

    private final AuditEventsDTO eventsForUser = new AuditEventsDTO(
            Arrays.asList(new AuditEventDTO("123", USER1_LOGIN, date("1993-03-09"), date("1993-03-09")),
                    new AuditEventDTO("456", USER1_LOGIN, date("1993-03-09"), date("1993-03-09"))),
            new Paging(userUri(NOT_ADMIN_USER_ID) + "?offset=456&limit=" + RequestParameters.DEFAULT_LIMIT),
            new HashMap<String, String>() {{
                put("self", userUri(NOT_ADMIN_USER_ID));
            }});

    private final AuditEventsDTO domainEventsWithTimeInterval = new AuditEventsDTO(
            Arrays.asList(new AuditEventDTO("123", USER1_LOGIN, date("1993-03-09"), date("1993-03-09")),
                    new AuditEventDTO("456", USER2_LOGIN, date("1995-03-09"), date("1995-03-09"))),
            new Paging(adminUri() + "?to=" + UPPER_BOUND + "&offset=456&limit=100"),
            new HashMap<String, String>() {{
                put("self", adminUri());
            }});

    @Before
    public void setUp() {
        final UserInfo adminUserInfo = new UserInfo(ADMIN_USER_ID, USER1_LOGIN, DOMAIN);
        final UserInfo notAdminUserInfo = new UserInfo(NOT_ADMIN_USER_ID, USER1_LOGIN, DOMAIN);

        doReturn(adminUserInfo).when(userDomainService).getUserInfo(ADMIN_USER_ID);
        doReturn(notAdminUserInfo).when(userDomainService).getUserInfo(NOT_ADMIN_USER_ID);

        doReturn(false).when(userDomainService).isUserDomainAdmin(NOT_ADMIN_USER_ID, DOMAIN);
        doReturn(true).when(userDomainService).isUserDomainAdmin(ADMIN_USER_ID, DOMAIN);

        doThrow(new UserNotDomainAdminException("")).when(userDomainService).authorizeAdmin(NOT_ADMIN_USER_ID, DOMAIN);

        RequestParameters pageRequestWithBadOffset = new RequestParameters();
        pageRequestWithBadOffset.setOffset(BAD_OFFSET);

        RequestParameters pageRequestDefault = new RequestParameters();

        when(auditEventService.findByDomain(eq(DOMAIN), eq(pageRequestDefault))).thenReturn(domainEvents);

        when(auditEventService.findByUser(eq(adminUserInfo), eq(pageRequestDefault))).thenReturn(eventsForAdminUser);
        when(auditEventService.findByUser(eq(notAdminUserInfo), eq(pageRequestDefault))).thenReturn(eventsForUser);

        RequestParameters boundedRequestParameters = new RequestParameters();
        boundedRequestParameters.setFrom(LOWER_BOUND);
        boundedRequestParameters.setTo(UPPER_BOUND);
        when(auditEventService.findByDomain(eq(DOMAIN), eq(boundedRequestParameters))).thenReturn(domainEventsWithTimeInterval);

        RequestParameters lowerBoundRequestParameters = new RequestParameters();
        lowerBoundRequestParameters.setFrom(LOWER_BOUND);

        RequestParameters pageRequestWithOffset = new RequestParameters();
        pageRequestWithOffset.setOffset(OFFSET.toString());
        pageRequestWithOffset.setFrom(LOWER_BOUND);
    }

    @Test
    public void testListAuditEventsUserNotSpecified() throws Exception {
        mockMvc.perform(get(adminUri()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(UserNotSpecifiedException.class.getName())))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testListAuditEventsInvalidOffset() throws Exception {
        mockMvc.perform(get(adminUri())
                .param("offset", BAD_OFFSET)
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(ValidationException.class.getName())))
                .andExpect(jsonPath("$.error.message", is(INVALID_OFFSET_MSG)))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testListAuditEventsNotAdmin() throws Exception {
        mockMvc.perform(get(adminUri())
                .header(X_PUBLIC_USER_ID, NOT_ADMIN_USER_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.errorClass", is(UserNotDomainAdminException.class.getName())))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testListAuditEventsDefaultPaging() throws Exception {
        mockMvc.perform(get(adminUri())
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(IOUtils.toString(getClass().getResourceAsStream("auditEvents.json"))));
    }

    @Test
    public void testListAuditEventsForUserNotSpecified() throws Exception {
        mockMvc.perform(get(userUri(ADMIN_USER_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(UserNotSpecifiedException.class.getName())))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testListAuditEventsForUserInvalidOffset() throws Exception {
        mockMvc.perform(get(userUri(ADMIN_USER_ID))
                .param("offset", BAD_OFFSET)
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(ValidationException.class.getName())))
                .andExpect(jsonPath("$.error.message", is(INVALID_OFFSET_MSG)))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testListAuditEventsForUserDefaultPaging() throws Exception {
        mockMvc.perform(get(userUri(ADMIN_USER_ID))
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(IOUtils.toString(getClass().getResourceAsStream("adminUserAuditEvents.json"))));
    }

    @Test
    public void testListAuditEventsWithTimeInterval() throws Exception {
        mockMvc.perform(get(adminUri())
                .param("from", LOWER_BOUND.toString())
                .param("to", UPPER_BOUND.toString())
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(IOUtils.toString(getClass().getResourceAsStream("auditEventsWithTimeInterval.json"))));
    }

    @Test
    public void testListAuditEventsWithBadLimit() throws Exception {
        String wrongValue = "not number";
        String errorMessage = format(TYPE_MISMATCH_MESSAGE, wrongValue, "limit");

        mockMvc.perform(get(adminUri())
                .param("limit", wrongValue)
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(ValidationException.class.getName())))
                .andExpect(jsonPath("$.error.message", is(errorMessage)))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testListAuditEventsForUserWithBadLimit() throws Exception {
        String wrongValue = "not number";
        String errorMessage = format(TYPE_MISMATCH_MESSAGE, wrongValue, "limit");

        mockMvc.perform(get(userUri(ADMIN_USER_ID))
                .param("limit", wrongValue)
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(ValidationException.class.getName())))
                .andExpect(jsonPath("$.error.message", is(errorMessage)))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testListAuditEventsInvalidFrom() throws Exception {
        String wrongValue = "a";
        String errorMessage = format(TYPE_MISMATCH_MESSAGE, wrongValue, "from");

        mockMvc.perform(get(adminUri())
                .param("from", wrongValue)
                .param("to", UPPER_BOUND.toString())
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(ValidationException.class.getName())))
                .andExpect(jsonPath("$.error.message", is(errorMessage)))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testListAuditEventsInvalidTo() throws Exception {
        String wrongValue = "a";
        String errorMessage = format(TYPE_MISMATCH_MESSAGE, wrongValue, "to");

        mockMvc.perform(get(adminUri())
                .param("from", LOWER_BOUND.toString())
                .param("to", wrongValue)
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(ValidationException.class.getName())))
                .andExpect(jsonPath("$.error.message", is(errorMessage)))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testListAuditEventsForUserInvalidFrom() throws Exception {
        String wrongValue = "a";
        String errorMessage = format(TYPE_MISMATCH_MESSAGE, wrongValue, "from");

        mockMvc.perform(get(userUri(ADMIN_USER_ID))
                .param("from", wrongValue)
                .param("to", UPPER_BOUND.toString())
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(ValidationException.class.getName())))
                .andExpect(jsonPath("$.error.message", is(errorMessage)))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testListAuditEventsForUserInvalidTo() throws Exception {
        String wrongValue = "a";
        String errorMessage = format(TYPE_MISMATCH_MESSAGE, wrongValue, "to");

        mockMvc.perform(get(userUri(ADMIN_USER_ID))
                .param("from", LOWER_BOUND.toString())
                .param("to", wrongValue)
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(ValidationException.class.getName())))
                .andExpect(jsonPath("$.error.message", is(errorMessage)))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testListAuditEventsExpectedContentType() throws Exception {
        mockMvc.perform(get(adminUri())
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", APPLICATION_JSON_VALUE + ";charset=UTF-8"));
    }

    @Test
    public void testListAuditEventsForUserExpectedContentType() throws Exception {
        mockMvc.perform(get(userUri(ADMIN_USER_ID))
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", APPLICATION_JSON_VALUE + ";charset=UTF-8"));
    }

    @Test
    public void testListAuditEventsFromAndOffsetSpecified() throws Exception {
        mockMvc.perform(get(adminUri())
                .param("offset", OFFSET.toString())
                .param("from", LOWER_BOUND.toString())
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(ValidationException.class.getName())))
                .andExpect(jsonPath("$.error.message", is(OFFSET_AND_FROM_SPECIFIED_MSG)))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testListAuditEventsForUserFromAndOffsetSpecified() throws Exception {
        mockMvc.perform(get(userUri(ADMIN_USER_ID))
                .param("offset", OFFSET.toString())
                .param("from", LOWER_BOUND.toString())
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(ValidationException.class.getName())))
                .andExpect(jsonPath("$.error.message", is(OFFSET_AND_FROM_SPECIFIED_MSG)))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testListAuditEventsForUserInvalidTimeInterval() throws Exception {
        mockMvc.perform(get(adminUri())
                .param("from", UPPER_BOUND.toString())
                .param("to", LOWER_BOUND.toString())
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(ValidationException.class.getName())))
                .andExpect(jsonPath("$.error.message", is(INVALID_TIME_INTERVAL_MSG)))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testListAuditEventsInvalidTimeInterval() throws Exception {
        mockMvc.perform(get(adminUri())
                .param("from", UPPER_BOUND.toString())
                .param("to", LOWER_BOUND.toString())
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(ValidationException.class.getName())))
                .andExpect(jsonPath("$.error.message", is(INVALID_TIME_INTERVAL_MSG)))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testListAuditEventsForUserWithNegativeLimit() throws Exception {
        Integer wrongValue = -1;

        mockMvc.perform(get(userUri(ADMIN_USER_ID))
                .param("limit", wrongValue.toString())
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(ValidationException.class.getName())))
                .andExpect(jsonPath("$.error.message", is(NOT_POSITIVE_LIMIT_MSG)))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testListAuditEventsForUserWithZeroLimit() throws Exception {
        Integer wrongValue = 0;

        mockMvc.perform(get(userUri(ADMIN_USER_ID))
                .param("limit", wrongValue.toString())
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(ValidationException.class.getName())))
                .andExpect(jsonPath("$.error.message", is(NOT_POSITIVE_LIMIT_MSG)))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testListAuditEventsWithNegativeLimit() throws Exception {
        Integer wrongValue = -1;

        mockMvc.perform(get(adminUri())
                .param("limit", wrongValue.toString())
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(ValidationException.class.getName())))
                .andExpect(jsonPath("$.error.message", is(NOT_POSITIVE_LIMIT_MSG)))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testListAuditEventsWithZeroLimit() throws Exception {
        Integer wrongValue = 0;

        mockMvc.perform(get(adminUri())
                .param("limit", wrongValue.toString())
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.errorClass", is(ValidationException.class.getName())))
                .andExpect(jsonPath("$.error.message", is(NOT_POSITIVE_LIMIT_MSG)))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testUserAccessingUserApiOfDifferentUser() throws Exception {
        mockMvc.perform(get(userUri(ADMIN_USER_ID))
                .header(X_PUBLIC_USER_ID, NOT_ADMIN_USER_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.errorClass", is(UserNotAuthorizedException.class.getName())))
                .andExpect(jsonPath("$.error.component", is(COMPONENT_NAME)));
    }

    @Test
    public void testAdminAccessingUserApiOfDifferentUser() throws Exception {
        mockMvc.perform(get(userUri(NOT_ADMIN_USER_ID))
                .header(X_PUBLIC_USER_ID, ADMIN_USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(IOUtils.toString(getClass().getResourceAsStream("userAuditEvents.json"))));
    }

    private String adminUri() {
        return ADMIN_URI_TEMPLATE.expand(DOMAIN).toString();
    }

    private String userUri(final String userId) {
        return USER_URI_TEMPLATE.expand(userId).toString();
    }
}
