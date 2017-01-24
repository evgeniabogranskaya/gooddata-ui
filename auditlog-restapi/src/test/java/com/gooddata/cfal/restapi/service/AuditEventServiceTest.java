/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gooddata.cfal.restapi.util.EntityDTOIdMatcher;
import com.gooddata.collections.PageRequest;
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

public class AuditEventServiceTest {

    private static final String DOMAIN = "domain";
    private static final String USER_ID = "user";

    private static final Integer CUSTOM_LIMIT = 2;

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

        when(auditLogEventRepository.findByDomain(DOMAIN, PageRequest.DEFAULT_LIMIT, null)).thenReturn(asList(event1, event2, event3));
        when(auditLogEventRepository.findByDomain(DOMAIN, CUSTOM_LIMIT, null)).thenReturn(asList(event1, event2));
        when(auditLogEventRepository.findByDomain(DOMAIN, CUSTOM_LIMIT, event2.getId())).thenReturn(singletonList(event3));
        when(auditLogEventRepository.findByDomainAndUser(DOMAIN, USER_ID, PageRequest.DEFAULT_LIMIT, null)).thenReturn(asList(event1, event2, event3));
        when(auditLogEventRepository.findByDomainAndUser(DOMAIN, USER_ID, CUSTOM_LIMIT, null)).thenReturn(asList(event1, event2));
        when(auditLogEventRepository.findByDomainAndUser(DOMAIN, USER_ID, CUSTOM_LIMIT, event2.getId())).thenReturn(singletonList(event3));
    }

    @Test
    public void testFindByDomainOnePage() {
        AuditEventsDTO events = auditEventService.findByDomain(DOMAIN, new PageRequest());

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2), EntityDTOIdMatcher.hasSameIdAs(event3)));
    }

    @Test
    public void testFindByDomainMultiplePages() {
        AuditEventsDTO firstPage = auditEventService.findByDomain(DOMAIN, new PageRequest(null, CUSTOM_LIMIT));

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2)));

        AuditEventsDTO secondPage = auditEventService.findByDomain(DOMAIN, new PageRequest(event2.getId().toString(), CUSTOM_LIMIT));

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage, Matchers.contains(EntityDTOIdMatcher.hasSameIdAs(event3)));
    }

    @Test
    public void testFindByDomainAndUserOnePage() {
        AuditEventsDTO events = auditEventService.findByDomainAndUser(DOMAIN, USER_ID, new PageRequest());

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2), EntityDTOIdMatcher.hasSameIdAs(event3)));
    }

    @Test
    public void testFindByDomainAndUserMultiplePages() {
        AuditEventsDTO firstPage = auditEventService.findByDomainAndUser(DOMAIN, USER_ID, new PageRequest(null, CUSTOM_LIMIT));

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage, containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2)));

        AuditEventsDTO secondPage = auditEventService.findByDomainAndUser(DOMAIN, USER_ID, new PageRequest(event2.getId().toString(), CUSTOM_LIMIT));

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage, Matchers.contains(EntityDTOIdMatcher.hasSameIdAs(event3)));
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

        when(event1.getTimestamp()).thenReturn(new DateTime());
        when(event2.getTimestamp()).thenReturn(new DateTime());
        when(event3.getTimestamp()).thenReturn(new DateTime());
    }
}