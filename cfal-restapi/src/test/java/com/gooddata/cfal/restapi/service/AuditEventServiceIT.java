/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.service;

import static com.gooddata.cfal.restapi.dto.AuditEventDTO.ADMIN_URI;
import static com.gooddata.cfal.restapi.dto.AuditEventDTO.USER_URI;
import static com.gooddata.cfal.restapi.util.DateUtils.convertDateTimeToObjectId;
import static com.gooddata.cfal.restapi.util.DateUtils.date;
import static com.gooddata.cfal.restapi.util.EntityDTOIdMatcher.hasSameIdAs;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.cfal.restapi.dto.AuditEventsDTO;
import com.gooddata.cfal.restapi.model.AuditEvent;
import com.gooddata.cfal.restapi.repository.AuditLogEventRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations="classpath:application-test.properties")
public class AuditEventServiceIT {

    private static final String DOMAIN = RandomStringUtils.randomAlphabetic(10);

    private static final String USER1 = RandomStringUtils.randomAlphabetic(10);
    private static final String USER2 = RandomStringUtils.randomAlphabetic(10);

    @Autowired
    private AuditLogEventRepository auditLogEventRepository;

    @Autowired
    private AuditEventService auditEventService;

    private AuditEvent event1;
    private AuditEvent event2;
    private AuditEvent event3;

    @Before
    public void setUp() {
        auditLogEventRepository.deleteAllByDomain(DOMAIN);

        event1 = new AuditEvent(convertDateTimeToObjectId(date("1993-03-09")), DOMAIN, USER1, date("1993-03-09"));
        event2 = new AuditEvent(convertDateTimeToObjectId(date("2001-03-09")), DOMAIN, USER2, date("2001-03-09"));
        event3 = new AuditEvent(convertDateTimeToObjectId(date("2015-03-09")), DOMAIN, USER1, date("2015-03-09"));

        auditLogEventRepository.save(event1);
        auditLogEventRepository.save(event2);
        auditLogEventRepository.save(event3);
    }

    @Test
    public void testFindByDomain() {
        RequestParameters requestParameters = new RequestParameters();
        AuditEventsDTO events = auditEventService.findByDomain(DOMAIN, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, Matchers.containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event2), hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainMultiplePages() {
        RequestParameters firstPageReq = new RequestParameters();
        firstPageReq.setLimit(2);
        AuditEventsDTO firstPage = auditEventService.findByDomain(DOMAIN, firstPageReq);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage, Matchers.containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event2)));
        assertThat(firstPage.getPaging().getNextUri(),
                is(ADMIN_URI + "?offset=" + event2.getId() + "&limit=" + firstPageReq.getSanitizedLimit()));

        RequestParameters secondPageReq = new RequestParameters();
        secondPageReq.setLimit(2);
        secondPageReq.setOffset(event2.getId().toString());
        AuditEventsDTO secondPage = auditEventService.findByDomain(DOMAIN, secondPageReq);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage, Matchers.contains(hasSameIdAs(event3)));
        assertThat(secondPage.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainAndUser() {
        RequestParameters pageReq = new RequestParameters();
        AuditEventsDTO events = auditEventService.findByDomainAndUser(DOMAIN, USER1, pageReq);

        assertThat(events, is(notNullValue()));
        assertThat(events, Matchers.containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainAndUserMultiplePages() {
        RequestParameters firstPageReq = new RequestParameters();
        firstPageReq.setLimit(1);
        AuditEventsDTO firstPage = auditEventService.findByDomainAndUser(DOMAIN, USER1, firstPageReq);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage, Matchers.contains(hasSameIdAs(event1)));
        assertThat(firstPage.getPaging().getNextUri(),
                is(USER_URI + "?offset=" + event1.getId() + "&limit=" + firstPageReq.getSanitizedLimit()));

        RequestParameters secondPageReq = new RequestParameters();
        secondPageReq.setLimit(1);
        secondPageReq.setOffset(event1.getId().toString());
        AuditEventsDTO secondPage = auditEventService.findByDomainAndUser(DOMAIN, USER1, secondPageReq);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage, Matchers.contains(hasSameIdAs(event3)));
        assertThat(secondPage.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainAndUserWithTimeIntervalFromAndTo() {
        DateTime from = date("1990-01-01");
        DateTime to = date("2005-01-01");

        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setFrom(from);
        requestParameters.setTo(to);

        AuditEventsDTO events = auditEventService.findByDomainAndUser(DOMAIN, USER1,  requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, Matchers.contains(hasSameIdAs(event1)));
        assertThat(events.getPaging().getNextUri(), is(Matchers.nullValue()));
    }

    @Test
    public void testFindByDomainAndUserWithTimeIntervalFrom() {
        DateTime from = date("1990-01-01");

        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setFrom(from);

        AuditEventsDTO events = auditEventService.findByDomainAndUser(DOMAIN, USER1, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, Matchers.contains(hasSameIdAs(event1), hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainAndUserWithTimeIntervalTo() {
        DateTime to = date("2005-01-01");

        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setTo(to);

        AuditEventsDTO events = auditEventService.findByDomainAndUser(DOMAIN, USER1, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, Matchers.contains(hasSameIdAs(event1)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainInvalidTimeInterval() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(10);
        requestParameters.setFrom(date("2005-01-01"));
        requestParameters.setTo(date("2000-01-01"));

        AuditEventsDTO events = auditEventService.findByDomain(DOMAIN, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, hasSize(0));
    }

    @Test
    public void testFindByDomainAndUserInvalidTimeInterval() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(10);
        requestParameters.setFrom(date("2005-01-01"));
        requestParameters.setTo(date("2000-01-01"));

        AuditEventsDTO events = auditEventService.findByDomainAndUser(DOMAIN, USER1, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, hasSize(0));
    }
}
