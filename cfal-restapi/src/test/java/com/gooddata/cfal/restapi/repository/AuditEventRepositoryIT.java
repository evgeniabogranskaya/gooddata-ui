/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.repository;

import static com.gooddata.cfal.restapi.util.DateUtils.convertDateTimeToObjectId;
import static com.gooddata.cfal.restapi.util.DateUtils.date;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.cfal.restapi.model.AuditEvent;
import com.gooddata.cfal.restapi.util.EntityIdMatcher;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.types.ObjectId;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations="classpath:application-test.properties")
public class AuditEventRepositoryIT {

    private static final String DOMAIN1 = RandomStringUtils.randomAlphabetic(10);
    private static final String DOMAIN2 = RandomStringUtils.randomAlphabetic(10);

    private static final String USER1 = RandomStringUtils.randomAlphabetic(10);
    private static final String USER2 = RandomStringUtils.randomAlphabetic(10);

    @Autowired
    private AuditLogEventRepository auditLogEventRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private AuditEvent event1;
    private AuditEvent event2;
    private AuditEvent event3;

    @Before
    public void setUp() {
        mongoTemplate.remove(new Query(), auditLogEventRepository.getMongoCollectionPrefix() + DOMAIN1);
        mongoTemplate.remove(new Query(), auditLogEventRepository.getMongoCollectionPrefix() + DOMAIN2);

        event1 = new AuditEvent(convertDateTimeToObjectId(date("1993-03-09")), DOMAIN1, USER1, date("1993-03-09"));
        event2 = new AuditEvent(convertDateTimeToObjectId(date("2001-03-09")), DOMAIN1, USER2, date("2001-03-09"));
        event3 = new AuditEvent(convertDateTimeToObjectId(date("2010-03-09")), DOMAIN1, USER1, date("2010-03-09"));

        mongoTemplate.save(event1, auditLogEventRepository.getMongoCollectionPrefix() + DOMAIN1);
        mongoTemplate.save(event2, auditLogEventRepository.getMongoCollectionPrefix() + DOMAIN1);
        mongoTemplate.save(event3, auditLogEventRepository.getMongoCollectionPrefix() + DOMAIN1);
    }

    @Test
    public void testFindByDomain() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(10);

        List<AuditEvent> events = auditLogEventRepository.findByDomain(DOMAIN1, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityIdMatcher.hasSameIdAs(event1), EntityIdMatcher.hasSameIdAs(event2), EntityIdMatcher.hasSameIdAs(event3)));
    }

    @Test
    public void testFindByDomainHitPageLimit() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(2);

        List<AuditEvent> events = auditLogEventRepository.findByDomain(DOMAIN1, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityIdMatcher.hasSameIdAs(event1), EntityIdMatcher.hasSameIdAs(event2)));
    }

    @Test
    public void testFindByDomainNextPage() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(2);
        requestParameters.setOffset(event2.getId().toString());

        List<AuditEvent> events = auditLogEventRepository.findByDomain(DOMAIN1, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, Matchers.contains(EntityIdMatcher.hasSameIdAs(event3)));
    }

    @Test
    public void testFindByDomainWithNotExistentOffset() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(2);
        requestParameters.setOffset(new ObjectId().toString());

        List<AuditEvent> events = auditLogEventRepository.findByDomain(DOMAIN1, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, hasSize(0));
    }

    @Test
    public void testSave() {
        AuditEvent test = new AuditEvent(DOMAIN2, USER1, new DateTime());

        auditLogEventRepository.save(test);

        assertThat(test.getId(), is(notNullValue()));
    }

    @Test
    public void testDeleteAll() {
        auditLogEventRepository.save(new AuditEvent(DOMAIN2, USER2, new DateTime()));

        auditLogEventRepository.deleteAllByDomain(DOMAIN2);

        assertThat(mongoTemplate.findAll(AuditEvent.class, DOMAIN2), hasSize(0));
    }

    @Test
    public void testFindByDomainAndUser() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(10);

        List<AuditEvent> eventsUser1 = auditLogEventRepository.findByDomainAndUser(DOMAIN1, USER1, requestParameters);

        assertThat(eventsUser1, is(notNullValue()));
        assertThat(eventsUser1, containsInAnyOrder(EntityIdMatcher.hasSameIdAs(event1), EntityIdMatcher.hasSameIdAs(event3)));

        List<AuditEvent> eventsUser2 = auditLogEventRepository.findByDomainAndUser(DOMAIN1, USER2, requestParameters);

        assertThat(eventsUser2, is(notNullValue()));
        assertThat(eventsUser2, Matchers.contains(EntityIdMatcher.hasSameIdAs(event2)));
    }

    @Test
    public void testFindByDomainAndUserWithOffset() {
        RequestParameters requestParameters1 = new RequestParameters();
        requestParameters1.setLimit(10);
        requestParameters1.setOffset(event1.getId().toString());

        List<AuditEvent> eventsUser1 = auditLogEventRepository.findByDomainAndUser(DOMAIN1, USER1, requestParameters1);

        assertThat(eventsUser1, is(notNullValue()));
        assertThat(eventsUser1, Matchers.contains(EntityIdMatcher.hasSameIdAs(event3)));

        RequestParameters requestParameters2 = new RequestParameters();
        requestParameters2.setLimit(10);
        requestParameters2.setOffset(event2.getId().toString());

        List<AuditEvent> eventsUser2 = auditLogEventRepository.findByDomainAndUser(DOMAIN1, USER2, requestParameters2);

        assertThat(eventsUser2, is(notNullValue()));
        assertThat(eventsUser2, hasSize(0));
    }

    @Test
    public void testFindByDomainAndUserMultiplePages() {
        RequestParameters requestParametersFirstPage = new RequestParameters();
        requestParametersFirstPage.setLimit(1);

        List<AuditEvent> firstPage = auditLogEventRepository.findByDomainAndUser(DOMAIN1, USER1, requestParametersFirstPage);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage, Matchers.contains(EntityIdMatcher.hasSameIdAs(event1)));

        RequestParameters requestParametersSecondPage = new RequestParameters();
        requestParametersSecondPage.setLimit(1);
        requestParametersSecondPage.setOffset(event1.getId().toString());

        List<AuditEvent> secondPage = auditLogEventRepository.findByDomainAndUser(DOMAIN1, USER1, requestParametersSecondPage);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage, Matchers.contains(EntityIdMatcher.hasSameIdAs(event3)));

        RequestParameters requestParametersThirdPage = new RequestParameters();
        requestParametersThirdPage.setLimit(1);
        requestParametersThirdPage.setOffset(event3.getId().toString());

        List<AuditEvent> thirdPage = auditLogEventRepository.findByDomainAndUser(DOMAIN1, USER1, requestParametersThirdPage);

        assertThat(thirdPage, is(notNullValue()));
        assertThat(thirdPage, hasSize(0));
    }

    @Test
    public void testFindByDomainWithTimeRangeFrom() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(10);
        requestParameters.setFrom(date("2000-01-01"));

        List<AuditEvent> events = auditLogEventRepository.findByDomain(DOMAIN1, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, containsInAnyOrder(EntityIdMatcher.hasSameIdAs(event2), EntityIdMatcher.hasSameIdAs(event3)));
    }

    @Test
    public void testFindByDomainWithTimeRangeTo() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(10);
        requestParameters.setTo(date("2000-01-01"));

        List<AuditEvent> events = auditLogEventRepository.findByDomain(DOMAIN1, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, contains(EntityIdMatcher.hasSameIdAs(event1)));
    }

    @Test
    public void testFindByDomainWithTimeRangeFromAndTo() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(10);
        requestParameters.setFrom(date("2000-01-01"));
        requestParameters.setTo(date("2005-01-01"));

        List<AuditEvent> events = auditLogEventRepository.findByDomain(DOMAIN1, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, contains(EntityIdMatcher.hasSameIdAs(event2)));
    }

    @Test
    public void testFindByDomainInvalidTimeInterval() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(10);
        requestParameters.setFrom(date("2005-01-01"));
        requestParameters.setTo(date("2000-01-01"));

        List<AuditEvent> events = auditLogEventRepository.findByDomain(DOMAIN1, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, hasSize(0));
    }

    @Test
    public void testFindByDomainAndUserInvalidTimeInterval() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(10);
        requestParameters.setFrom(date("2005-01-01"));
        requestParameters.setTo(date("2000-01-01"));

        List<AuditEvent> events = auditLogEventRepository.findByDomainAndUser(DOMAIN1, USER1, requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, hasSize(0));
    }

    public void testFindByDomainEventsAreOrdered() {
        AuditEvent auditEvent1 = new AuditEvent(convertDateTimeToObjectId(date("1993-03-09")), DOMAIN2, USER1, new DateTime());
        AuditEvent auditEvent2 = new AuditEvent(convertDateTimeToObjectId(date("1994-03-09")), DOMAIN2, USER1, new DateTime());
        AuditEvent auditEvent3 = new AuditEvent(convertDateTimeToObjectId(date("2000-03-09")), DOMAIN2, USER1, new DateTime());
        AuditEvent auditEvent4 = new AuditEvent(convertDateTimeToObjectId(date("2001-03-09")), DOMAIN2, USER1, new DateTime());
        AuditEvent auditEvent5 = new AuditEvent(convertDateTimeToObjectId(date("2010-03-09")), DOMAIN2, USER1, new DateTime());

        //persist in random order
        auditLogEventRepository.save(auditEvent3);
        auditLogEventRepository.save(auditEvent1);
        auditLogEventRepository.save(auditEvent5);
        auditLogEventRepository.save(auditEvent2);
        auditLogEventRepository.save(auditEvent4);


        List<AuditEvent> events = auditLogEventRepository.findByDomain(DOMAIN2, new RequestParameters());

        assertThat(events, contains(EntityIdMatcher.hasSameIdAs(auditEvent1),
                EntityIdMatcher.hasSameIdAs(auditEvent2),
                EntityIdMatcher.hasSameIdAs(auditEvent3),
                EntityIdMatcher.hasSameIdAs(auditEvent4),
                EntityIdMatcher.hasSameIdAs(auditEvent5)));
    }

    @Test
    public void testFindByDomainAndUserEventsAreOrdered() {
        AuditEvent auditEvent1 = new AuditEvent(convertDateTimeToObjectId(date("1993-03-09")), DOMAIN2, USER1, new DateTime());
        AuditEvent auditEvent2 = new AuditEvent(convertDateTimeToObjectId(date("1994-03-09")), DOMAIN2, USER1, new DateTime());
        AuditEvent auditEvent3 = new AuditEvent(convertDateTimeToObjectId(date("2000-03-09")), DOMAIN2, USER1, new DateTime());
        AuditEvent auditEvent4 = new AuditEvent(convertDateTimeToObjectId(date("2001-03-09")), DOMAIN2, USER1, new DateTime());
        AuditEvent auditEvent5 = new AuditEvent(convertDateTimeToObjectId(date("2010-03-09")), DOMAIN2, USER1, new DateTime());

        //persist in random order
        auditLogEventRepository.save(auditEvent3);
        auditLogEventRepository.save(auditEvent1);
        auditLogEventRepository.save(auditEvent5);
        auditLogEventRepository.save(auditEvent2);
        auditLogEventRepository.save(auditEvent4);

        List<AuditEvent> events = auditLogEventRepository.findByDomainAndUser(DOMAIN2, USER1, new RequestParameters());

        assertThat(events, contains(EntityIdMatcher.hasSameIdAs(auditEvent1),
                EntityIdMatcher.hasSameIdAs(auditEvent2),
                EntityIdMatcher.hasSameIdAs(auditEvent3),
                EntityIdMatcher.hasSameIdAs(auditEvent4),
                EntityIdMatcher.hasSameIdAs(auditEvent5)));
    }

    @Test
    public void testStringToDateTimeConversion() {
        DateTime expectedDate = date("1993-03-09");
        TestEntity objectToSave = new TestEntity(expectedDate.toString());

        mongoTemplate.save(objectToSave, auditLogEventRepository.getMongoCollectionPrefix() + DOMAIN2);

        List<AuditEvent> result = auditLogEventRepository.findByDomain(DOMAIN2, new RequestParameters());

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), is(equalTo(objectToSave.getId())));
        assertThat(result.get(0).getRealTimeOccurrence(), is(equalTo(expectedDate)));
    }

    /**
     * Test entity for String to DateTime conversion
     */
    static class TestEntity {

        @Id
        private ObjectId id;

        private String realTimeOccurrence;

        public TestEntity(final String realTimeOccurrence) {
            this.realTimeOccurrence = realTimeOccurrence;
        }

        public ObjectId getId() {
            return id;
        }

        public String getRealTimeOccurrence() {
            return realTimeOccurrence;
        }
    }
}