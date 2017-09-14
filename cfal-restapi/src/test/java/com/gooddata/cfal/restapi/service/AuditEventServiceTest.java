/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.service;

import static com.gooddata.auditevent.AuditEvent.ADMIN_URI_TEMPLATE;
import static com.gooddata.auditevent.AuditEvent.USER_URI_TEMPLATE;
import static com.gooddata.cfal.restapi.util.DateUtils.date;
import static com.gooddata.cfal.restapi.util.EntityDTOIdMatcher.hasSameIdAs;
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

import com.gooddata.auditevent.AuditEventPageRequest;
import com.gooddata.cfal.restapi.dto.UserInfo;
import com.gooddata.cfal.restapi.util.EntityDTOIdMatcher;
import com.gooddata.auditevent.AuditEvents;
import com.gooddata.cfal.restapi.model.AuditEventEntity;
import com.gooddata.cfal.restapi.repository.AuditLogEventRepository;
import org.bson.types.ObjectId;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AuditEventServiceTest {

    private static final String DOMAIN = "domain";
    private static final String USER_ID = "123";
    private static final String USER_LOGIN = "bear@gooddata.com";
    private static final String IP = "127.0.0.1";
    private static final boolean SUCCESS = true;
    private static final String TYPE = "login";

    private static final Integer CUSTOM_LIMIT = 2;

    private static final DateTime LOWER_BOUND = date("1995-01-01");
    private static final DateTime UPPER_BOUND = date("2000-01-01");

    private static final UserInfo USER_INFO = new UserInfo(USER_ID, USER_LOGIN, DOMAIN);

    private static final Map<String, String> EMPTY_PARAMS = new HashMap<>();

    private AuditEventPageRequest requestParamLBUB;
    private AuditEventPageRequest requestParamLB;
    private AuditEventPageRequest requestParamUB;
    private AuditEventPageRequest requestParamWithCustomLimit;
    private AuditEventPageRequest requestParamWithCustomLimitAndOffset;
    private AuditEventPageRequest requestParam;

    @Mock
    private AuditLogEventRepository auditLogEventRepository;

    private AuditEventService auditEventService;

    private AuditEventEntity event1;
    private AuditEventEntity event2;
    private AuditEventEntity event3;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        auditEventService = new AuditEventService(auditLogEventRepository);

        mockEvents();

        prepareRequestParams();

        AuditEventPageRequest repositoryRequestParamLBUB = requestParamLBUB.withIncrementedLimit();
        AuditEventPageRequest repositoryRequestParamLB = requestParamLB.withIncrementedLimit();
        AuditEventPageRequest repositoryRequestParamUB = requestParamUB.withIncrementedLimit();
        AuditEventPageRequest repositoryRequestParamWithCustomLimit = requestParamWithCustomLimit.withIncrementedLimit();
        AuditEventPageRequest repositoryRequestParamWithCustomLimitAndOffset = requestParamWithCustomLimitAndOffset.withIncrementedLimit();
        AuditEventPageRequest repositoryRequestParam = requestParam.withIncrementedLimit();

        when(auditLogEventRepository.findByDomain(DOMAIN, repositoryRequestParam)).thenReturn(asList(event1, event2, event3));
        when(auditLogEventRepository.findByDomain(DOMAIN, repositoryRequestParamWithCustomLimit)).thenReturn(asList(event1, event2, event3));
        when(auditLogEventRepository.findByDomain(DOMAIN, repositoryRequestParamWithCustomLimitAndOffset)).thenReturn(singletonList(event3));

        when(auditLogEventRepository.findByUser(USER_INFO, repositoryRequestParam)).thenReturn(asList(event1, event2, event3));
        when(auditLogEventRepository.findByUser(USER_INFO, repositoryRequestParamWithCustomLimit)).thenReturn(asList(event1, event2, event3));
        when(auditLogEventRepository.findByUser(USER_INFO, repositoryRequestParamWithCustomLimitAndOffset)).thenReturn(singletonList(event3));

        when(auditLogEventRepository.findByDomain(DOMAIN, repositoryRequestParamLB)).thenReturn(asList(event2, event3));
        when(auditLogEventRepository.findByDomain(DOMAIN, repositoryRequestParamUB)).thenReturn(asList(event1, event2));
        when(auditLogEventRepository.findByDomain(DOMAIN, repositoryRequestParamLBUB)).thenReturn(singletonList(event2));

        when(auditLogEventRepository.findByUser(USER_INFO, repositoryRequestParamLB)).thenReturn(asList(event2, event3));
        when(auditLogEventRepository.findByUser(USER_INFO, repositoryRequestParamUB)).thenReturn(asList(event1, event2));
        when(auditLogEventRepository.findByUser(USER_INFO, repositoryRequestParamLBUB)).thenReturn(singletonList(event2));
    }

    @Test
    public void testFindByDomainOnePage() {
        AuditEvents events = auditEventService.findByDomain(DOMAIN, requestParam);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event2), hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainMultiplePages() {
        String uri = ADMIN_URI_TEMPLATE.expand(DOMAIN).toString();
        AuditEvents firstPage = auditEventService.findByDomain(DOMAIN, requestParamWithCustomLimit);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage, containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event2)));
        assertThat(firstPage.getPaging().getNextUri(),
                is(format("%s?offset=%s&limit=%d", uri, event2.getId(), requestParamWithCustomLimit.getSanitizedLimit())));

        AuditEvents secondPage = auditEventService.findByDomain(DOMAIN, requestParamWithCustomLimitAndOffset);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage, Matchers.contains(hasSameIdAs(event3)));
        assertThat(secondPage.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByUserOnePage() {
        AuditEvents events = auditEventService.findByUser(USER_INFO, requestParam);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event2), hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByUserMultiplePages() {
        String uri = USER_URI_TEMPLATE.expand(USER_ID).toString();
        AuditEvents firstPage = auditEventService.findByUser(USER_INFO, requestParamWithCustomLimit);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage, containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event2)));
        assertThat(firstPage.getPaging().getNextUri(),
                is(format("%s?offset=%s&limit=%d", uri, event2.getId(), requestParamWithCustomLimit.getSanitizedLimit())));

        AuditEvents secondPage = auditEventService.findByUser(USER_INFO, requestParamWithCustomLimitAndOffset);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage, Matchers.contains(hasSameIdAs(event3)));
        assertThat(secondPage.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainOnePageWithTimeIntervalFrom() {
        AuditEvents events = auditEventService.findByDomain(DOMAIN, requestParamLB);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(hasSameIdAs(event2), hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainOnePageWithTimeIntervalTo() {
        AuditEvents events = auditEventService.findByDomain(DOMAIN, requestParamUB);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event2)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByDomainOnePageWithTimeIntervalFromAndTo() {
        AuditEvents events = auditEventService.findByDomain(DOMAIN, requestParamLBUB);

        assertThat(events, is(notNullValue()));
        assertThat(events, contains(hasSameIdAs(event2)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByUserOnePageWithTimeIntervalFrom() {
        AuditEvents events = auditEventService.findByUser(USER_INFO, requestParamLB);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(hasSameIdAs(event2), hasSameIdAs(event3)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByUserOnePageWithTimeIntervalTo() {
        AuditEvents events = auditEventService.findByUser(USER_INFO, requestParamUB);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event2)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testFindByUserOnePageWithTimeIntervalFromAndTo() {
        AuditEvents events = auditEventService.findByUser(USER_INFO, requestParamLBUB);

        assertThat(events, is(notNullValue()));
        assertThat(events, contains(hasSameIdAs(event2)));
        assertThat(events.getPaging().getNextUri(), is(nullValue()));
    }

    private void mockEvents() {
        event1 = mock(AuditEventEntity.class);
        event2 = mock(AuditEventEntity.class);
        event3 = mock(AuditEventEntity.class);

        when(event1.getId()).thenReturn(new ObjectId());
        when(event2.getId()).thenReturn(new ObjectId());
        when(event3.getId()).thenReturn(new ObjectId());

        when(event1.getDomainId()).thenReturn(DOMAIN);
        when(event2.getDomainId()).thenReturn(DOMAIN);
        when(event3.getDomainId()).thenReturn(DOMAIN);

        when(event1.getUserLogin()).thenReturn(USER_ID);
        when(event2.getUserLogin()).thenReturn(USER_ID);
        when(event3.getUserLogin()).thenReturn(USER_ID);

        when(event1.getOccurred()).thenReturn(date("1993-03-09"));
        when(event2.getOccurred()).thenReturn(date("1998-01-01"));
        when(event3.getOccurred()).thenReturn(date("2016-01-01"));

        when(event1.getUserIp()).thenReturn(IP);
        when(event2.getUserIp()).thenReturn(IP);
        when(event3.getUserIp()).thenReturn(IP);

        when(event1.isSuccess()).thenReturn(SUCCESS);
        when(event2.isSuccess()).thenReturn(SUCCESS);
        when(event3.isSuccess()).thenReturn(SUCCESS);

        when(event1.getType()).thenReturn(TYPE);
        when(event2.getType()).thenReturn(TYPE);
        when(event3.getType()).thenReturn(TYPE);

        when(event1.getParams()).thenReturn(EMPTY_PARAMS);
        when(event2.getParams()).thenReturn(EMPTY_PARAMS);
        when(event3.getParams()).thenReturn(EMPTY_PARAMS);
    }

    private void prepareRequestParams() {
        requestParamLBUB = new AuditEventPageRequest();
        requestParamLBUB.setFrom(LOWER_BOUND);
        requestParamLBUB.setTo(UPPER_BOUND);

        requestParamLB = new AuditEventPageRequest();
        requestParamLB.setFrom(LOWER_BOUND);

        requestParamUB = new AuditEventPageRequest();
        requestParamUB.setTo(UPPER_BOUND);

        requestParamWithCustomLimit = new AuditEventPageRequest();
        requestParamWithCustomLimit.setLimit(CUSTOM_LIMIT);

        requestParamWithCustomLimitAndOffset = new AuditEventPageRequest();
        requestParamWithCustomLimitAndOffset.setLimit(CUSTOM_LIMIT);
        requestParamWithCustomLimitAndOffset.setOffset(event2.getId().toString());

        requestParam = new AuditEventPageRequest();
    }
}