/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.service;

import static com.gooddata.auditevent.AuditEvent.ADMIN_URI_TEMPLATE;
import static com.gooddata.auditevent.AuditEvent.USER_URI_TEMPLATE;
import static com.gooddata.cfal.restapi.util.DateUtils.convertDateTimeToObjectId;
import static com.gooddata.cfal.restapi.util.DateUtils.date;
import static com.gooddata.cfal.restapi.util.EntityDTOIdMatcher.hasSameIdAs;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.gooddata.auditevent.AuditEventPageRequest;
import com.gooddata.auditevent.AuditEvents;
import com.gooddata.cfal.restapi.dto.UserInfo;
import com.gooddata.cfal.restapi.model.AuditEventEntity;
import com.gooddata.cfal.restapi.repository.AuditLogEventRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations="classpath:application-test.properties")
public class AuditEventServiceIT {

    private static final String DOMAIN = RandomStringUtils.randomAlphabetic(10);

    private static final String USER1_ID = RandomStringUtils.randomAlphabetic(10);
    private static final String USER2_ID = RandomStringUtils.randomAlphabetic(10);

    private static final String USER1_LOGIN = "bear@gooddata.com";
    private static final String USER2_LOGIN = "jane@gooddata.com";

    private static final UserInfo USER1_INFO = new UserInfo(USER1_ID, USER1_LOGIN, DOMAIN);

    private static final String IP = "127.0.0.1";
    private static final boolean SUCCESS = true;
    private static final String TYPE = "login";
    private static final Map<String, String> EMPTY_PARAMS = new HashMap<>();
    private static final Map<String, String> EMPTY_LINKS = new HashMap<>();

    @Autowired
    private AuditLogEventRepository auditLogEventRepository;

    @Autowired
    private AuditEventService auditEventService;

    @MockBean
    private UserDomainService userDomainService;

    private AuditEventEntity event1;
    private AuditEventEntity event2;
    private AuditEventEntity event3;

    @Before
    public void setUp() {
        auditLogEventRepository.deleteAllByDomain(DOMAIN);

        event1 = new AuditEventEntity(convertDateTimeToObjectId(date("1993-03-09")), DOMAIN, USER1_LOGIN, date("1993-03-09"), IP, SUCCESS, TYPE, EMPTY_PARAMS, EMPTY_LINKS);
        event2 = new AuditEventEntity(convertDateTimeToObjectId(date("2001-03-09")), DOMAIN, USER2_LOGIN, date("2001-03-09"), IP, SUCCESS, TYPE, EMPTY_PARAMS, EMPTY_LINKS);
        event3 = new AuditEventEntity(convertDateTimeToObjectId(date("2015-03-09")), DOMAIN, USER1_LOGIN, date("2015-03-09"), IP, SUCCESS, TYPE, EMPTY_PARAMS, EMPTY_LINKS);

        auditLogEventRepository.save(event1);
        auditLogEventRepository.save(event2);
        auditLogEventRepository.save(event3);
    }

    @Test
    public void testFindByDomain() {
        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        AuditEvents events = auditEventService.findByDomain(DOMAIN, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, Matchers.containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event2), hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainMultiplePages() {
        String uri = ADMIN_URI_TEMPLATE.expand(DOMAIN).toString();
        AuditEventPageRequest firstPageReq = new AuditEventPageRequest();
        firstPageReq.setLimit(2);
        AuditEvents firstPage = auditEventService.findByDomain(DOMAIN, firstPageReq);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage, Matchers.containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event2)));
        assertThat(firstPage.getPaging().getNextUri(),
                is(uri + "?offset=" + event2.getId() + "&limit=" + firstPageReq.getSanitizedLimit()));

        AuditEventPageRequest secondPageReq = new AuditEventPageRequest();
        secondPageReq.setLimit(2);
        secondPageReq.setOffset(event2.getId().toString());
        AuditEvents secondPage = auditEventService.findByDomain(DOMAIN, secondPageReq);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage, Matchers.contains(hasSameIdAs(event3)));
        assertThat(secondPage.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByUser() {
        AuditEventPageRequest pageReq = new AuditEventPageRequest();
        AuditEvents events = auditEventService.findByUser(USER1_INFO, pageReq);

        assertThat(events, is(notNullValue()));
        assertThat(events, Matchers.containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByUserMultiplePages() {
        String uri = USER_URI_TEMPLATE.expand(USER1_ID).toString();
        AuditEventPageRequest firstPageReq = new AuditEventPageRequest();
        firstPageReq.setLimit(1);
        AuditEvents firstPage = auditEventService.findByUser(USER1_INFO, firstPageReq);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage, Matchers.contains(hasSameIdAs(event1)));
        assertThat(firstPage.getPaging().getNextUri(),
                is(uri + "?offset=" + event1.getId() + "&limit=" + firstPageReq.getSanitizedLimit()));

        AuditEventPageRequest secondPageReq = new AuditEventPageRequest();
        secondPageReq.setLimit(1);
        secondPageReq.setOffset(event1.getId().toString());
        AuditEvents secondPage = auditEventService.findByUser(USER1_INFO, secondPageReq);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage, Matchers.contains(hasSameIdAs(event3)));
        assertThat(secondPage.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByUserWithTimeIntervalFromAndTo() {
        DateTime from = date("1990-01-01");
        DateTime to = date("2005-01-01");

        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setFrom(from);
        requestParameters.setTo(to);

        AuditEvents events = auditEventService.findByUser(USER1_INFO, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, Matchers.contains(hasSameIdAs(event1)));
        assertThat(events.getPaging().getNextUri(), is(Matchers.nullValue()));
    }

    @Test
    public void testFindByUserWithTimeIntervalFrom() {
        DateTime from = date("1990-01-01");

        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setFrom(from);

        AuditEvents events = auditEventService.findByUser(USER1_INFO, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, Matchers.contains(hasSameIdAs(event1), hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByUserWithTimeIntervalTo() {
        DateTime to = date("2005-01-01");

        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setTo(to);

        AuditEvents events = auditEventService.findByUser(USER1_INFO, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, Matchers.contains(hasSameIdAs(event1)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainInvalidTimeInterval() {
        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setLimit(10);
        requestParameters.setFrom(date("2005-01-01"));
        requestParameters.setTo(date("2000-01-01"));

        AuditEvents events = auditEventService.findByDomain(DOMAIN, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, hasSize(0));
    }

    @Test
    public void testFindByUserInvalidTimeInterval() {
        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setLimit(10);
        requestParameters.setFrom(date("2005-01-01"));
        requestParameters.setTo(date("2000-01-01"));

        AuditEvents events = auditEventService.findByUser(USER1_INFO, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, hasSize(0));
    }
}
