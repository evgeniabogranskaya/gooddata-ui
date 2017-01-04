/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.service;

import static com.gooddata.util.EntityDTOIdMatcher.hasSameIdAs;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.gooddata.collections.PageRequest;
import com.gooddata.dto.AuditEventsDTO;
import com.gooddata.model.AuditEvent;
import com.gooddata.repository.AuditEventRepository;
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
@SpringBootTest
@TestPropertySource(locations="classpath:application-test.properties")
public class AuditEventServiceIT {

    private static final String DOMAIN = RandomStringUtils.randomAlphabetic(10);

    private static final String USER1 = RandomStringUtils.randomAlphabetic(10);
    private static final String USER2 = RandomStringUtils.randomAlphabetic(10);

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private AuditEventService auditEventService;

    private AuditEvent event1;
    private AuditEvent event2;
    private AuditEvent event3;

    @Before
    public void setUp() {
        auditEventRepository.deleteAllByDomain(DOMAIN);

        event1 = new AuditEvent(DOMAIN, USER1, new DateTime());
        event2 = new AuditEvent(DOMAIN, USER2, new DateTime());
        event3 = new AuditEvent(DOMAIN, USER1, new DateTime());

        auditEventRepository.save(event1);
        auditEventRepository.save(event2);
        auditEventRepository.save(event3);
    }

    @Test
    public void testFindByDomain() {
        AuditEventsDTO events = auditEventService.findByDomain(DOMAIN, new PageRequest());

        assertThat(events, is(notNullValue()));
        assertThat(events, Matchers.containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event2), hasSameIdAs(event3)));
    }

    @Test
    public void testFindByDomainMultiplePages() {
        AuditEventsDTO firstPage = auditEventService.findByDomain(DOMAIN, new PageRequest(null, 2));

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage, Matchers.containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event2)));

        AuditEventsDTO secondPage = auditEventService.findByDomain(DOMAIN, new PageRequest(event2.getId().toString(), 2));

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage, Matchers.contains(hasSameIdAs(event3)));
    }

    @Test
    public void testFindByDomainAndUser() {
        AuditEventsDTO events = auditEventService.findByDomainAndUser(DOMAIN, USER1, new PageRequest());

        assertThat(events, is(notNullValue()));
        assertThat(events, Matchers.containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event3)));
    }

    @Test
    public void testFindByDomainAndUserMultiplePages() {
        AuditEventsDTO firstPage = auditEventService.findByDomainAndUser(DOMAIN, USER1, new PageRequest(null, 1));

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage, Matchers.contains(hasSameIdAs(event1)));

        AuditEventsDTO secondPage = auditEventService.findByDomainAndUser(DOMAIN, USER1, new PageRequest(event1.getId().toString(), 1));

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage, Matchers.contains(hasSameIdAs(event3)));
    }
}
