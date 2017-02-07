/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.service;

import static com.gooddata.cfal.restapi.dto.AuditEventDTO.ADMIN_URI;
import static com.gooddata.cfal.restapi.dto.AuditEventDTO.USER_URI;
import static com.gooddata.cfal.restapi.util.DateUtils.date;
import static java.lang.String.format;
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

    private RequestParameters requestParamLBUB;
    private RequestParameters requestParamLB;
    private RequestParameters requestParamUB;

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

        requestParamLBUB = new RequestParameters();
        requestParamLBUB.setFrom(LOWER_BOUND);
        requestParamLBUB.setTo(UPPER_BOUND);

        requestParamLB = new RequestParameters();
        requestParamLB.setFrom(LOWER_BOUND);

        requestParamUB = new RequestParameters();
        requestParamUB.setTo(UPPER_BOUND);

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

        when(auditLogEventRepository.findByDomain(DOMAIN, requestParamLB)).thenReturn(asList(event2, event3));
        when(auditLogEventRepository.findByDomain(DOMAIN, requestParamUB)).thenReturn(asList(event1, event2));
        when(auditLogEventRepository.findByDomain(DOMAIN, requestParamLBUB)).thenReturn(Collections.singletonList(event2));

        when(auditLogEventRepository.findByDomainAndUser(DOMAIN, USER_ID, requestParamLB)).thenReturn(asList(event2, event3));
        when(auditLogEventRepository.findByDomainAndUser(DOMAIN, USER_ID, requestParamUB)).thenReturn(asList(event1, event2));
        when(auditLogEventRepository.findByDomainAndUser(DOMAIN, USER_ID, requestParamLBUB)).thenReturn(singletonList(event2));
    }

    @Test
    public void testFindByDomainOnePage() {
        RequestParameters pageReq = new RequestParameters();
        AuditEventsDTO events = auditEventService.findByDomain(DOMAIN, pageReq);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2), EntityDTOIdMatcher.hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(),
                is(format("%s?offset=%s&limit=%d", ADMIN_URI, event3.getId(), pageReq.getSanitizedLimit())));
    }

    @Test
    public void testFindByDomainMultiplePages() {
        RequestParameters firstPageReq = new RequestParameters();
        firstPageReq.setLimit(CUSTOM_LIMIT);
        AuditEventsDTO firstPage = auditEventService.findByDomain(DOMAIN, firstPageReq);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2)));
        assertThat(firstPage.getPaging().getNextUri(),
                is(format("%s?offset=%s&limit=%d", ADMIN_URI, event2.getId(), firstPageReq.getSanitizedLimit())));

        RequestParameters secondPageReq = new RequestParameters();
        secondPageReq.setLimit(CUSTOM_LIMIT);
        secondPageReq.setOffset(event2.getId().toString());
        AuditEventsDTO secondPage = auditEventService.findByDomain(DOMAIN, secondPageReq);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage, Matchers.contains(EntityDTOIdMatcher.hasSameIdAs(event3)));
        assertThat(secondPage.getPaging().getNextUri(),
                is(format("%s?offset=%s&limit=%d", ADMIN_URI, event3.getId(), secondPageReq.getSanitizedLimit())));
    }

    @Test
    public void testFindByDomainAndUserOnePage() {
        RequestParameters pageReq = new RequestParameters();
        AuditEventsDTO events = auditEventService.findByDomainAndUser(DOMAIN, USER_ID, pageReq);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2), EntityDTOIdMatcher.hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(),
                is(format("%s?offset=%s&limit=%d", USER_URI, event3.getId(), pageReq.getSanitizedLimit())));
    }

    @Test
    public void testFindByDomainAndUserMultiplePages() {
        RequestParameters firstPageReq = new RequestParameters();
        firstPageReq.setLimit(CUSTOM_LIMIT);
        AuditEventsDTO firstPage = auditEventService.findByDomainAndUser(DOMAIN, USER_ID, firstPageReq);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2)));
        assertThat(firstPage.getPaging().getNextUri(),
                is(format("%s?offset=%s&limit=%d", USER_URI, event2.getId(), firstPageReq.getSanitizedLimit())));

        RequestParameters secondPageReq = new RequestParameters();
        secondPageReq.setOffset(event2.getId().toString());
        secondPageReq.setLimit(CUSTOM_LIMIT);
        AuditEventsDTO secondPage = auditEventService.findByDomainAndUser(DOMAIN, USER_ID, secondPageReq);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage, Matchers.contains(EntityDTOIdMatcher.hasSameIdAs(event3)));
        assertThat(secondPage.getPaging().getNextUri(),
                is(format("%s?offset=%s&limit=%d", USER_URI, event3.getId(), secondPageReq.getSanitizedLimit())));
    }

    @Test
    public void testFindByDomainOnePageWithTimeIntervalFrom() {
        AuditEventsDTO events = auditEventService.findByDomain(DOMAIN, requestParamLB);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event2), EntityDTOIdMatcher.hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(),
                is(format("%s?offset=%s&limit=%d", ADMIN_URI, event3.getId(), requestParamLB.getSanitizedLimit())));
    }

    @Test
    public void testFindByDomainOnePageWithTimeIntervalTo() {
        AuditEventsDTO events = auditEventService.findByDomain(DOMAIN, requestParamUB);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2)));
        assertThat(events.getPaging().getNextUri(),
                is(format("%s?to=%s&offset=%s&limit=%d", ADMIN_URI, UPPER_BOUND, event2.getId(), requestParamUB.getSanitizedLimit())));
    }

    @Test
    public void testFindByDomainOnePageWithTimeIntervalFromAndTo() {
        AuditEventsDTO events = auditEventService.findByDomain(DOMAIN, requestParamLBUB);

        assertThat(events, is(notNullValue()));
        assertThat(events, contains(EntityDTOIdMatcher.hasSameIdAs(event2)));
        assertThat(events.getPaging().getNextUri(),
                is(format("%s?to=%s&offset=%s&limit=%d", ADMIN_URI, UPPER_BOUND, event2.getId(), requestParamLBUB.getSanitizedLimit())));
    }

    @Test
    public void testFindByDomainAndUserOnePageWithTimeIntervalFrom() {
        AuditEventsDTO events = auditEventService.findByDomainAndUser(DOMAIN, USER_ID, requestParamLB);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event2), EntityDTOIdMatcher.hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(),
                is(format("%s?offset=%s&limit=%d", USER_URI, event3.getId(), requestParamLB.getSanitizedLimit())));
    }

    @Test
    public void testFindByDomainAndUserOnePageWithTimeIntervalTo() {
        AuditEventsDTO events = auditEventService.findByDomainAndUser(DOMAIN, USER_ID, requestParamUB);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2)));
        assertThat(events.getPaging().getNextUri(),
                is(format("%s?to=%s&offset=%s&limit=%d", USER_URI, UPPER_BOUND, event2.getId(), requestParamUB.getSanitizedLimit())));
    }

    @Test
    public void testFindByDomainAndUserOnePageWithTimeIntervalFromAndTo() {
        AuditEventsDTO events = auditEventService.findByDomainAndUser(DOMAIN, USER_ID, requestParamLBUB);

        assertThat(events, is(notNullValue()));
        assertThat(events, contains(EntityDTOIdMatcher.hasSameIdAs(event2)));
        assertThat(events.getPaging().getNextUri(),
                is(format("%s?to=%s&offset=%s&limit=%d", USER_URI, UPPER_BOUND, event2.getId(), requestParamLBUB.getSanitizedLimit())));
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