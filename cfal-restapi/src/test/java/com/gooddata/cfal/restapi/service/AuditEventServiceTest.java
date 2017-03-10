/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.service;

import static com.gooddata.cfal.restapi.dto.AuditEventDTO.ADMIN_URI_TEMPLATE;
import static com.gooddata.cfal.restapi.dto.AuditEventDTO.USER_URI_TEMPLATE;
import static com.gooddata.cfal.restapi.util.DateUtils.date;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.cfal.restapi.dto.UserInfo;
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
    private static final String USER_ID = "123";
    private static final String USER_LOGIN = "bear@gooddata.com";

    private static final Integer CUSTOM_LIMIT = 2;

    private static final DateTime LOWER_BOUND = date("1995-01-01");
    private static final DateTime UPPER_BOUND = date("2000-01-01");

    private static final UserInfo USER_INFO = new UserInfo(USER_ID, USER_LOGIN, DOMAIN);

    private RequestParameters requestParamLBUB;
    private RequestParameters requestParamLB;
    private RequestParameters requestParamUB;
    private RequestParameters requestParamWithCustomLimit;
    private RequestParameters requestParamWithCustomLimitAndOffset;
    private RequestParameters requestParam;

    @Mock
    private AuditLogEventRepository auditLogEventRepository;

    @Mock
    private UserDomainService userDomainService;

    private AuditEventService auditEventService;

    private AuditEvent event1;
    private AuditEvent event2;
    private AuditEvent event3;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        auditEventService = new AuditEventService(auditLogEventRepository, userDomainService);

        mockEvents();

        prepareRequestParams();

        RequestParameters repositoryRequestParamLBUB = requestParamLBUB.withIncrementedLimit();
        RequestParameters repositoryRequestParamLB = requestParamLB.withIncrementedLimit();
        RequestParameters repositoryRequestParamUB = requestParamUB.withIncrementedLimit();
        RequestParameters repositoryRequestParamWithCustomLimit = requestParamWithCustomLimit.withIncrementedLimit();
        RequestParameters repositoryRequestParamWithCustomLimitAndOffset = requestParamWithCustomLimitAndOffset.withIncrementedLimit();
        RequestParameters repositoryRequestParam = requestParam.withIncrementedLimit();

        when(auditLogEventRepository.findByDomain(DOMAIN, repositoryRequestParam)).thenReturn(asList(event1, event2, event3));
        when(auditLogEventRepository.findByDomain(DOMAIN, repositoryRequestParamWithCustomLimit)).thenReturn(asList(event1, event2, event3));
        when(auditLogEventRepository.findByDomain(DOMAIN, repositoryRequestParamWithCustomLimitAndOffset)).thenReturn(singletonList(event3));

        when(auditLogEventRepository.findByUser(USER_INFO, repositoryRequestParam)).thenReturn(asList(event1, event2, event3));
        when(auditLogEventRepository.findByUser(USER_INFO, repositoryRequestParamWithCustomLimit)).thenReturn(asList(event1, event2, event3));
        when(auditLogEventRepository.findByUser(USER_INFO, repositoryRequestParamWithCustomLimitAndOffset)).thenReturn(singletonList(event3));

        when(auditLogEventRepository.findByDomain(DOMAIN, repositoryRequestParamLB)).thenReturn(asList(event2, event3));
        when(auditLogEventRepository.findByDomain(DOMAIN, repositoryRequestParamUB)).thenReturn(asList(event1, event2));
        when(auditLogEventRepository.findByDomain(DOMAIN, repositoryRequestParamLBUB)).thenReturn(Collections.singletonList(event2));

        when(auditLogEventRepository.findByUser(USER_INFO, repositoryRequestParamLB)).thenReturn(asList(event2, event3));
        when(auditLogEventRepository.findByUser(USER_INFO, repositoryRequestParamUB)).thenReturn(asList(event1, event2));
        when(auditLogEventRepository.findByUser(USER_INFO, repositoryRequestParamLBUB)).thenReturn(singletonList(event2));
    }

    @Test
    public void testFindByDomainOnePage() {
        AuditEventsDTO events = auditEventService.findByDomain(DOMAIN, requestParam);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2), EntityDTOIdMatcher.hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainMultiplePages() {
        String uri = ADMIN_URI_TEMPLATE.expand(DOMAIN).toString();
        AuditEventsDTO firstPage = auditEventService.findByDomain(DOMAIN, requestParamWithCustomLimit);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2)));
        assertThat(firstPage.getPaging().getNextUri(),
                is(format("%s?offset=%s&limit=%d", uri, event2.getId(), requestParamWithCustomLimit.getSanitizedLimit())));

        AuditEventsDTO secondPage = auditEventService.findByDomain(DOMAIN, requestParamWithCustomLimitAndOffset);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage, Matchers.contains(EntityDTOIdMatcher.hasSameIdAs(event3)));
        assertThat(secondPage.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainAndUserOnePage() {
        AuditEventsDTO events = auditEventService.findByUser(USER_INFO, requestParam);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2), EntityDTOIdMatcher.hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainAndUserMultiplePages() {
        String uri = USER_URI_TEMPLATE.expand(USER_ID).toString();
        AuditEventsDTO firstPage = auditEventService.findByUser(USER_INFO, requestParamWithCustomLimit);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2)));
        assertThat(firstPage.getPaging().getNextUri(),
                is(format("%s?offset=%s&limit=%d", uri, event2.getId(), requestParamWithCustomLimit.getSanitizedLimit())));

        AuditEventsDTO secondPage = auditEventService.findByUser(USER_INFO, requestParamWithCustomLimitAndOffset);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage, Matchers.contains(EntityDTOIdMatcher.hasSameIdAs(event3)));
        assertThat(secondPage.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainOnePageWithTimeIntervalFrom() {
        AuditEventsDTO events = auditEventService.findByDomain(DOMAIN, requestParamLB);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event2), EntityDTOIdMatcher.hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainOnePageWithTimeIntervalTo() {
        AuditEventsDTO events = auditEventService.findByDomain(DOMAIN, requestParamUB);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainOnePageWithTimeIntervalFromAndTo() {
        AuditEventsDTO events = auditEventService.findByDomain(DOMAIN, requestParamLBUB);

        assertThat(events, is(notNullValue()));
        assertThat(events, contains(EntityDTOIdMatcher.hasSameIdAs(event2)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainAndUserOnePageWithTimeIntervalFrom() {
        AuditEventsDTO events = auditEventService.findByUser(USER_INFO, requestParamLB);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event2), EntityDTOIdMatcher.hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainAndUserOnePageWithTimeIntervalTo() {
        AuditEventsDTO events = auditEventService.findByUser(USER_INFO, requestParamUB);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainAndUserOnePageWithTimeIntervalFromAndTo() {
        AuditEventsDTO events = auditEventService.findByUser(USER_INFO, requestParamLBUB);

        assertThat(events, is(notNullValue()));
        assertThat(events, contains(EntityDTOIdMatcher.hasSameIdAs(event2)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
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

        when(event1.getUserLogin()).thenReturn(USER_ID);
        when(event2.getUserLogin()).thenReturn(USER_ID);
        when(event3.getUserLogin()).thenReturn(USER_ID);

        when(event1.getOccurred()).thenReturn(date("1993-03-09"));
        when(event2.getOccurred()).thenReturn(date("1998-01-01"));
        when(event3.getOccurred()).thenReturn(date("2016-01-01"));
    }

    private void prepareRequestParams() {
        requestParamLBUB = new RequestParameters();
        requestParamLBUB.setFrom(LOWER_BOUND);
        requestParamLBUB.setTo(UPPER_BOUND);

        requestParamLB = new RequestParameters();
        requestParamLB.setFrom(LOWER_BOUND);

        requestParamUB = new RequestParameters();
        requestParamUB.setTo(UPPER_BOUND);

        requestParamWithCustomLimit = new RequestParameters();
        requestParamWithCustomLimit.setLimit(CUSTOM_LIMIT);

        requestParamWithCustomLimitAndOffset = new RequestParameters();
        requestParamWithCustomLimitAndOffset.setLimit(CUSTOM_LIMIT);
        requestParamWithCustomLimitAndOffset.setOffset(event2.getId().toString());

        requestParam = new RequestParameters();
    }
}