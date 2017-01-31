/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.service;

import static com.gooddata.cfal.restapi.dto.AuditEventDTO.ADMIN_URI;
import static com.gooddata.cfal.restapi.dto.AuditEventDTO.USER_URI;
import static com.gooddata.cfal.restapi.util.DateUtils.date;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.cfal.restapi.exception.OffsetAndFromSpecifiedException;
import com.gooddata.cfal.restapi.util.EntityDTOIdMatcher;
import com.gooddata.cfal.restapi.dto.AuditEventsDTO;
import com.gooddata.cfal.restapi.model.AuditEvent;
import com.gooddata.cfal.restapi.repository.AuditLogEventRepository;
import org.bson.types.ObjectId;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

public class AuditEventServiceTest {

    private static final String DOMAIN = "domain";
    private static final String USER_ID = "user";

    private static final Integer CUSTOM_LIMIT = 2;

    private static final DateTime LOWER_BOUND = date("1995-01-01");

    private static final DateTime UPPER_BOUND = date("2000-01-01");

    @Mock
    private AuditLogEventRepository auditLogEventRepository;

    private AuditEventService auditEventService;

    private AuditEvent event1;
    private AuditEvent event2;
    private AuditEvent event3;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        auditEventService = new AuditEventService(auditLogEventRepository);

        mockEvents();

        RequestParameters lowerBoundRequestParameters = new RequestParameters();
        lowerBoundRequestParameters.setFrom(LOWER_BOUND);

        RequestParameters upperBoundRequestParameters = new RequestParameters();
        upperBoundRequestParameters.setTo(UPPER_BOUND);

        RequestParameters boundedRequestParameters = new RequestParameters();
        boundedRequestParameters.setFrom(LOWER_BOUND);
        boundedRequestParameters.setTo(UPPER_BOUND);

        RequestParameters requestParametersWithCustomLimit = new RequestParameters();
        requestParametersWithCustomLimit.setLimit(CUSTOM_LIMIT);

        RequestParameters requestParametersWithCustomLimitAndOffset = new RequestParameters();
        requestParametersWithCustomLimitAndOffset.setLimit(CUSTOM_LIMIT);
        requestParametersWithCustomLimitAndOffset.setOffset(event2.getId().toString());

        when(auditLogEventRepository.findByDomain(DOMAIN, new RequestParameters())).thenReturn(asList(event1, event2, event3));
        when(auditLogEventRepository.findByDomain(DOMAIN, requestParametersWithCustomLimit)).thenReturn(asList(event1, event2));
        when(auditLogEventRepository.findByDomain(DOMAIN, requestParametersWithCustomLimitAndOffset)).thenReturn(singletonList(event3));

        when(auditLogEventRepository.findByDomainAndUser(DOMAIN, USER_ID, new RequestParameters())).thenReturn(asList(event1, event2, event3));
        when(auditLogEventRepository.findByDomainAndUser(DOMAIN, USER_ID, requestParametersWithCustomLimit)).thenReturn(asList(event1, event2));
        when(auditLogEventRepository.findByDomainAndUser(DOMAIN, USER_ID, requestParametersWithCustomLimitAndOffset)).thenReturn(singletonList(event3));

        when(auditLogEventRepository.findByDomain(DOMAIN, lowerBoundRequestParameters)).thenReturn(asList(event2, event3));
        when(auditLogEventRepository.findByDomain(DOMAIN, upperBoundRequestParameters)).thenReturn(asList(event1, event2));
        when(auditLogEventRepository.findByDomain(DOMAIN, boundedRequestParameters)).thenReturn(Collections.singletonList(event2));

        when(auditLogEventRepository.findByDomainAndUser(DOMAIN, USER_ID, lowerBoundRequestParameters)).thenReturn(asList(event2, event3));
        when(auditLogEventRepository.findByDomainAndUser(DOMAIN, USER_ID, upperBoundRequestParameters)).thenReturn(asList(event1, event2));
        when(auditLogEventRepository.findByDomainAndUser(DOMAIN, USER_ID, boundedRequestParameters)).thenReturn(singletonList(event2));
    }

    @Test
    public void testFindByDomainOnePage() {
        RequestParameters pageReq = new RequestParameters();
        AuditEventsDTO events = auditEventService.findByDomain(DOMAIN, pageReq);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2), EntityDTOIdMatcher.hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(),
                is(ADMIN_URI + "?offset=" + event3.getId() + "&limit=" + pageReq.getSanitizedLimit()));
    }

    @Test
    public void testFindByDomainMultiplePages() {
        RequestParameters firstPageReq = new RequestParameters();
        firstPageReq.setLimit(CUSTOM_LIMIT);
        AuditEventsDTO firstPage = auditEventService.findByDomain(DOMAIN, firstPageReq);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2)));
        assertThat(firstPage.getPaging().getNextUri(),
                is(ADMIN_URI + "?offset=" + event2.getId() + "&limit=" + firstPageReq.getSanitizedLimit()));

        RequestParameters secondPageReq = new RequestParameters();
        secondPageReq.setLimit(CUSTOM_LIMIT);
        secondPageReq.setOffset(event2.getId().toString());
        AuditEventsDTO secondPage = auditEventService.findByDomain(DOMAIN, secondPageReq);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage, Matchers.contains(EntityDTOIdMatcher.hasSameIdAs(event3)));
        assertThat(secondPage.getPaging().getNextUri(),
                is(ADMIN_URI + "?offset=" + event3.getId() + "&limit=" + secondPageReq.getSanitizedLimit()));
    }

    @Test
    public void testFindByDomainAndUserOnePage() {
        RequestParameters pageReq = new RequestParameters();
        AuditEventsDTO events = auditEventService.findByDomainAndUser(DOMAIN, USER_ID, pageReq);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2), EntityDTOIdMatcher.hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(),
                is(USER_URI + "?offset=" + event3.getId() + "&limit=" + pageReq.getSanitizedLimit()));
    }

    @Test
    public void testFindByDomainAndUserMultiplePages() {
        RequestParameters firstPageReq = new RequestParameters();
        firstPageReq.setLimit(CUSTOM_LIMIT);
        AuditEventsDTO firstPage = auditEventService.findByDomainAndUser(DOMAIN, USER_ID, firstPageReq);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2)));
        assertThat(firstPage.getPaging().getNextUri(),
                is(USER_URI + "?offset=" + event2.getId() + "&limit=" + firstPageReq.getSanitizedLimit()));

        RequestParameters secondPageReq = new RequestParameters();
        secondPageReq.setOffset(event2.getId().toString());
        secondPageReq.setLimit(CUSTOM_LIMIT);
        AuditEventsDTO secondPage = auditEventService.findByDomainAndUser(DOMAIN, USER_ID, secondPageReq);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage, Matchers.contains(EntityDTOIdMatcher.hasSameIdAs(event3)));
        assertThat(secondPage.getPaging().getNextUri(),
                is(USER_URI + "?offset=" + event3.getId() + "&limit=" + secondPageReq.getSanitizedLimit()));
    }

    @Test
    public void testFindByDomainOnePageWithTimeIntervalFrom() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setFrom(LOWER_BOUND);

        AuditEventsDTO events = auditEventService.findByDomain(DOMAIN, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event2), EntityDTOIdMatcher.hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(),
                is(ADMIN_URI +"?offset=" + event3.getId() + "&limit=" + requestParameters.getSanitizedLimit()));
    }

    @Test
    public void testFindByDomainOnePageWithTimeIntervalTo() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setTo(UPPER_BOUND);

        AuditEventsDTO events = auditEventService.findByDomain(DOMAIN, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2)));
        assertThat(events.getPaging().getNextUri(),
                is(ADMIN_URI + "?to=" + UPPER_BOUND + "&offset=" + event2.getId() + "&limit=" + requestParameters.getSanitizedLimit()));
    }

    @Test
    public void testFindByDomainOnePageWithTimeIntervalFromAndTo() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setFrom(LOWER_BOUND);
        requestParameters.setTo(UPPER_BOUND);

        AuditEventsDTO events = auditEventService.findByDomain(DOMAIN, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, contains(EntityDTOIdMatcher.hasSameIdAs(event2)));
        assertThat(events.getPaging().getNextUri(),
                is(ADMIN_URI + "?to=" + UPPER_BOUND + "&offset=" + event2.getId() + "&limit=" + requestParameters.getSanitizedLimit()));
    }

    @Test
    public void testFindByDomainAndUserOnePageWithTimeIntervalFrom() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setFrom(LOWER_BOUND);

        AuditEventsDTO events = auditEventService.findByDomainAndUser(DOMAIN, USER_ID, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event2), EntityDTOIdMatcher.hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(),
                is(USER_URI + "?offset=" + event3.getId() + "&limit=" + requestParameters.getSanitizedLimit()));
    }

    @Test
    public void testFindByDomainAndUserOnePageWithTimeIntervalTo() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setTo(UPPER_BOUND);

        AuditEventsDTO events = auditEventService.findByDomainAndUser(DOMAIN, USER_ID, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2)));
        assertThat(events.getPaging().getNextUri(),
                is(USER_URI + "?to=" + UPPER_BOUND + "&offset=" + event2.getId() + "&limit=" + requestParameters.getSanitizedLimit()));
    }

    @Test
    public void testFindByDomainAndUserOnePageWithTimeIntervalFromAndTo() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setFrom(LOWER_BOUND);
        requestParameters.setTo(UPPER_BOUND);

        AuditEventsDTO events = auditEventService.findByDomainAndUser(DOMAIN, USER_ID, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, contains(EntityDTOIdMatcher.hasSameIdAs(event2)));
        assertThat(events.getPaging().getNextUri(),
                is(USER_URI + "?to=" + UPPER_BOUND + "&offset=" + event2.getId() + "&limit=" + requestParameters.getSanitizedLimit()));
    }

    private void mockEvents() {
        event1 = mock(AuditEvent.class);
        event2 = mock(AuditEvent.class);
        event3 = mock(AuditEvent.class);

        when(event1.getId()).thenReturn(new ObjectId());
        when(event2.getId()).thenReturn(new ObjectId());
        when(event3.getId()).thenReturn(new ObjectId());

        when(event1.getDomain()).thenReturn(DOMAIN);
        when(event2.getDomain()).thenReturn(DOMAIN);
        when(event3.getDomain()).thenReturn(DOMAIN);

        when(event1.getUserId()).thenReturn(USER_ID);
        when(event2.getUserId()).thenReturn(USER_ID);
        when(event3.getUserId()).thenReturn(USER_ID);

        when(event1.getRealTimeOccurrence()).thenReturn(date("1993-03-09"));
        when(event2.getRealTimeOccurrence()).thenReturn(date("1998-01-01"));
        when(event3.getRealTimeOccurrence()).thenReturn(date("2016-01-01"));
    }
}