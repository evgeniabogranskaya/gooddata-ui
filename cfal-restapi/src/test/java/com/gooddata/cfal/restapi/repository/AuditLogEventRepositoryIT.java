/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.repository;

import com.gooddata.cfal.restapi.config.MonitoringTestConfig;
import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.cfal.restapi.dto.UserInfo;
import com.gooddata.cfal.restapi.model.AuditEvent;
import com.gooddata.cfal.restapi.util.EntityIdMatcher;
import com.mongodb.DBObject;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.types.ObjectId;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.gooddata.cfal.restapi.util.DateUtils.convertDateTimeToObjectId;
import static com.gooddata.cfal.restapi.util.DateUtils.date;
import static java.util.concurrent.TimeUnit.DAYS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(MonitoringTestConfig.class)
public class AuditLogEventRepositoryIT {

    private static final String DOMAIN1 = RandomStringUtils.randomAlphabetic(10);
    private static final String DOMAIN2 = RandomStringUtils.randomAlphabetic(10);

    private static final String USER1_ID = RandomStringUtils.randomAlphabetic(10);
    private static final String USER2_ID = RandomStringUtils.randomAlphabetic(10);

    private static final String USER1_LOGIN = RandomStringUtils.randomAlphabetic(10);
    private static final String USER2_LOGIN = RandomStringUtils.randomAlphabetic(10);

    private static final String IP = "127.0.0.1";
    private static final boolean SUCCESS = true;
    private static final String TYPE = "login";

    @Autowired
    private AuditLogEventRepository auditLogEventRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private AuditEvent event1;
    private AuditEvent event2;
    private AuditEvent event3;

    @Before
    public void setUp() {
        mongoTemplate.remove(new Query(), auditLogEventRepository.getMongoCollectionName(DOMAIN1));
        mongoTemplate.remove(new Query(), auditLogEventRepository.getMongoCollectionName(DOMAIN2));

        event1 = new AuditEvent(convertDateTimeToObjectId(date("1993-03-09")), DOMAIN1, USER1_LOGIN, date("1993-03-09"), IP, SUCCESS, TYPE);
        event2 = new AuditEvent(convertDateTimeToObjectId(date("2001-03-09")), DOMAIN1, USER2_LOGIN, date("2001-03-09"), IP, SUCCESS, TYPE);
        event3 = new AuditEvent(convertDateTimeToObjectId(date("2010-03-09")), DOMAIN1, USER1_LOGIN, date("2010-03-09"), IP, SUCCESS, TYPE);

        mongoTemplate.save(event1, auditLogEventRepository.getMongoCollectionName(DOMAIN1));
        mongoTemplate.save(event2, auditLogEventRepository.getMongoCollectionName(DOMAIN1));
        mongoTemplate.save(event3, auditLogEventRepository.getMongoCollectionName(DOMAIN1));
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
        AuditEvent test = new AuditEvent(DOMAIN2, USER1_LOGIN, new DateTime(), IP, SUCCESS, TYPE);

        auditLogEventRepository.save(test);

        assertThat(test.getId(), is(notNullValue()));
    }

    @Test
    public void testDeleteAll() {
        auditLogEventRepository.save(new AuditEvent(DOMAIN2, USER2_LOGIN, new DateTime(), IP, SUCCESS, TYPE));

        auditLogEventRepository.deleteAllByDomain(DOMAIN2);

        assertThat(mongoTemplate.findAll(AuditEvent.class, DOMAIN2), hasSize(0));
    }

    @Test
    public void testFindByUser() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(10);

        List<AuditEvent> eventsUser1 = auditLogEventRepository.findByUser(new UserInfo(USER1_ID, USER1_LOGIN, DOMAIN1), requestParameters);

        assertThat(eventsUser1, is(notNullValue()));
        assertThat(eventsUser1, containsInAnyOrder(EntityIdMatcher.hasSameIdAs(event1), EntityIdMatcher.hasSameIdAs(event3)));

        List<AuditEvent> eventsUser2 = auditLogEventRepository.findByUser(new UserInfo(USER2_ID, USER2_LOGIN, DOMAIN1), requestParameters);

        assertThat(eventsUser2, is(notNullValue()));
        assertThat(eventsUser2, Matchers.contains(EntityIdMatcher.hasSameIdAs(event2)));
    }

    @Test
    public void testFindByUserWithOffset() {
        RequestParameters requestParameters1 = new RequestParameters();
        requestParameters1.setLimit(10);
        requestParameters1.setOffset(event1.getId().toString());

        List<AuditEvent> eventsUser1 = auditLogEventRepository.findByUser(new UserInfo(USER1_ID, USER1_LOGIN, DOMAIN1), requestParameters1);

        assertThat(eventsUser1, is(notNullValue()));
        assertThat(eventsUser1, Matchers.contains(EntityIdMatcher.hasSameIdAs(event3)));

        RequestParameters requestParameters2 = new RequestParameters();
        requestParameters2.setLimit(10);
        requestParameters2.setOffset(event2.getId().toString());

        List<AuditEvent> eventsUser2 = auditLogEventRepository.findByUser(new UserInfo(USER2_ID, USER2_LOGIN, DOMAIN1), requestParameters2);

        assertThat(eventsUser2, is(notNullValue()));
        assertThat(eventsUser2, hasSize(0));
    }

    @Test
    public void testFindByUserMultiplePages() {
        RequestParameters requestParametersFirstPage = new RequestParameters();
        requestParametersFirstPage.setLimit(1);

        UserInfo userInfo = new UserInfo(USER1_ID, USER1_LOGIN, DOMAIN1);
        List<AuditEvent> firstPage = auditLogEventRepository.findByUser(userInfo, requestParametersFirstPage);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage, Matchers.contains(EntityIdMatcher.hasSameIdAs(event1)));

        RequestParameters requestParametersSecondPage = new RequestParameters();
        requestParametersSecondPage.setLimit(1);
        requestParametersSecondPage.setOffset(event1.getId().toString());

        List<AuditEvent> secondPage = auditLogEventRepository.findByUser(userInfo, requestParametersSecondPage);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage, Matchers.contains(EntityIdMatcher.hasSameIdAs(event3)));

        RequestParameters requestParametersThirdPage = new RequestParameters();
        requestParametersThirdPage.setLimit(1);
        requestParametersThirdPage.setOffset(event3.getId().toString());

        List<AuditEvent> thirdPage = auditLogEventRepository.findByUser(userInfo, requestParametersThirdPage);

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
    public void testFindByUserInvalidTimeInterval() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(10);
        requestParameters.setFrom(date("2005-01-01"));
        requestParameters.setTo(date("2000-01-01"));

        List<AuditEvent> events = auditLogEventRepository.findByUser(new UserInfo(USER1_ID, USER1_LOGIN, DOMAIN1), requestParameters);

        assertThat(events, is(notNullValue()));
        assertThat(events, hasSize(0));
    }

    public void testFindByDomainEventsAreOrdered() {
        AuditEvent auditEvent1 = new AuditEvent(convertDateTimeToObjectId(date("1993-03-09")), DOMAIN2, USER1_LOGIN, new DateTime(), IP, SUCCESS, TYPE);
        AuditEvent auditEvent2 = new AuditEvent(convertDateTimeToObjectId(date("1994-03-09")), DOMAIN2, USER1_LOGIN, new DateTime(), IP, SUCCESS, TYPE);
        AuditEvent auditEvent3 = new AuditEvent(convertDateTimeToObjectId(date("2000-03-09")), DOMAIN2, USER1_LOGIN, new DateTime(), IP, SUCCESS, TYPE);
        AuditEvent auditEvent4 = new AuditEvent(convertDateTimeToObjectId(date("2001-03-09")), DOMAIN2, USER1_LOGIN, new DateTime(), IP, SUCCESS, TYPE);
        AuditEvent auditEvent5 = new AuditEvent(convertDateTimeToObjectId(date("2010-03-09")), DOMAIN2, USER1_LOGIN, new DateTime(), IP, SUCCESS, TYPE);

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
    public void testFindByUserEventsAreOrdered() {
        AuditEvent auditEvent1 = new AuditEvent(convertDateTimeToObjectId(date("1993-03-09")), DOMAIN2, USER1_LOGIN, new DateTime(), IP, SUCCESS, TYPE);
        AuditEvent auditEvent2 = new AuditEvent(convertDateTimeToObjectId(date("1994-03-09")), DOMAIN2, USER1_LOGIN, new DateTime(), IP, SUCCESS, TYPE);
        AuditEvent auditEvent3 = new AuditEvent(convertDateTimeToObjectId(date("2000-03-09")), DOMAIN2, USER1_LOGIN, new DateTime(), IP, SUCCESS, TYPE);
        AuditEvent auditEvent4 = new AuditEvent(convertDateTimeToObjectId(date("2001-03-09")), DOMAIN2, USER1_LOGIN, new DateTime(), IP, SUCCESS, TYPE);
        AuditEvent auditEvent5 = new AuditEvent(convertDateTimeToObjectId(date("2010-03-09")), DOMAIN2, USER1_LOGIN, new DateTime(), IP, SUCCESS, TYPE);

        //persist in random order
        auditLogEventRepository.save(auditEvent3);
        auditLogEventRepository.save(auditEvent1);
        auditLogEventRepository.save(auditEvent5);
        auditLogEventRepository.save(auditEvent2);
        auditLogEventRepository.save(auditEvent4);

        List<AuditEvent> events = auditLogEventRepository.findByUser(new UserInfo(USER1_ID, USER1_LOGIN, DOMAIN2), new RequestParameters());

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

        mongoTemplate.save(objectToSave, auditLogEventRepository.getMongoCollectionName(DOMAIN2));

        List<AuditEvent> result = auditLogEventRepository.findByDomain(DOMAIN2, new RequestParameters());

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), is(equalTo(objectToSave.getId())));
        assertThat(result.get(0).getOccurred(), is(equalTo(expectedDate)));
    }

    @Test
    public void testCreateTtlIndexes() {
        TestEntity objectToSave = new TestEntity(date("1993-03-09").toString());

        final String mongoCollectionName = auditLogEventRepository.getMongoCollectionName(DOMAIN2);
        mongoTemplate.save(objectToSave, mongoCollectionName);
        auditLogEventRepository.createTtlIndexes();

        final List<DBObject> indexInfo = mongoTemplate.getCollection(mongoCollectionName).getIndexInfo();
        // there should be at least one (new) index
        assertThat(indexInfo, hasSize(greaterThanOrEqualTo(1)));
        // make sure there's an index with 'expireAfterSeconds' set to 7-days on top of 'occurred' key
        assertThat(indexInfo, hasItem(Matchers
                .both(dbObjectMatch("expireAfterSeconds", is(DAYS.toSeconds(7))))
                .and(dbObjectMatch("key", dbObjectMatch("occurred", is(1))))));
    }

    @Test
    public void testCreateUserLoginIndexes() {
        TestEntity objectToSave = new TestEntity(date("1993-03-09").toString());

        final String mongoCollectionName = auditLogEventRepository.getMongoCollectionName(DOMAIN2);
        mongoTemplate.save(objectToSave, mongoCollectionName);
        auditLogEventRepository.createUserLoginIndexes();

        final List<DBObject> indexInfo = mongoTemplate.getCollection(mongoCollectionName).getIndexInfo();
        // there should be at least one (new) index
        assertThat(indexInfo, hasSize(greaterThanOrEqualTo(1)));
        // make sure there's an index with 'expireAfterSeconds' set to 7-days on top of 'occurred' key
        assertThat(indexInfo, hasItem(dbObjectMatch("key", dbObjectMatch("userLogin", is(1)))));
    }

    private <T> FeatureMatcher<DBObject, T> dbObjectMatch(String feature, Matcher<T> matcher) {
        return new FeatureMatcher<DBObject, T>(matcher, feature, feature) {
            @Override
            protected T featureValueOf(DBObject dbObject) {
                return (T) dbObject.get(feature);
            }
        };
    }

    /**
     * Test entity for String to DateTime conversion
     */
    static class TestEntity {

        @Id
        private ObjectId id;

        private String occurred;

        public TestEntity(final String occurred) {
            this.occurred = occurred;
        }

        public ObjectId getId() {
            return id;
        }

        public String getOccurred() {
            return occurred;
        }
    }
}