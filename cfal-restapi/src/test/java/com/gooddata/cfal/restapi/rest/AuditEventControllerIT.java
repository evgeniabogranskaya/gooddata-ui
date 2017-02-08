/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.rest;

import com.gooddata.c4.domain.C4Domain;
import com.gooddata.c4.domain.DomainService;
import com.gooddata.c4.user.C4User;
import com.gooddata.c4.user.UserService;
import com.gooddata.cfal.restapi.dto.AuditEventsDTO;
import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.cfal.restapi.model.AuditEvent;
import com.gooddata.cfal.restapi.repository.AuditLogEventRepository;
import com.gooddata.cfal.restapi.util.EntityDTOIdMatcher;
import com.gooddata.exception.servlet.ErrorStructure;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import static com.gooddata.cfal.restapi.dto.AuditEventDTO.ADMIN_URI;
import static com.gooddata.cfal.restapi.dto.AuditEventDTO.USER_URI;
import static com.gooddata.cfal.restapi.util.DateUtils.convertDateTimeToObjectId;
import static com.gooddata.cfal.restapi.util.DateUtils.date;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
public class AuditEventControllerIT {

    private static final String DOMAIN = RandomStringUtils.randomAlphabetic(10);

    private static final String USER1 = RandomStringUtils.randomAlphabetic(10);
    private static final String USER2 = RandomStringUtils.randomAlphabetic(10);

    private static final DateTime TIME_2000 = date("2000-03-09");
    private static final DateTime TIME_1990 = date("1990-01-01");
    private static final DateTime TIME_1995 = date("1995-01-01");

    @MockBean
    private UserService userService;

    @MockBean
    private DomainService domainService;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private AuditLogEventRepository auditLogEventRepository;

    private AuditEvent event1 = new AuditEvent(convertDateTimeToObjectId(date("1993-03-09")), DOMAIN, USER1, date("1993-03-09"));
    private AuditEvent event2 = new AuditEvent(convertDateTimeToObjectId(date("1995-03-09")), DOMAIN, USER2, date("1995-03-09"));
    private AuditEvent event3 = new AuditEvent(convertDateTimeToObjectId(date("1996-03-09")), DOMAIN, USER1, date("1996-03-09"));
    private AuditEvent event4 = new AuditEvent(convertDateTimeToObjectId(date("2001-03-09")), DOMAIN, USER1, date("2001-03-09"));
    private AuditEvent event5 = new AuditEvent(convertDateTimeToObjectId(date("2016-03-09")), DOMAIN, USER2, date("2016-03-09"));

    private C4User c4User1;
    private C4User c4User2;

    private C4Domain c4Domain;

    @Before
    public void setUp() {
        auditLogEventRepository.deleteAllByDomain(DOMAIN);

        auditLogEventRepository.save(event1);
        auditLogEventRepository.save(event2);
        auditLogEventRepository.save(event3);
        auditLogEventRepository.save(event3);
        auditLogEventRepository.save(event4);
        auditLogEventRepository.save(event5);

        c4User1 = mock(C4User.class);
        c4User2 = mock(C4User.class);

        doReturn("/gdc/c4/domain/" + DOMAIN).when(c4User1).getDomainUri();
        doReturn("/gdc/c4/domain/" + DOMAIN).when(c4User2).getDomainUri();

        doReturn(c4User1).when(userService).getUser(USER1);
        doReturn(c4User2).when(userService).getUser(USER2);

        c4Domain = mock(C4Domain.class);

        doReturn(c4Domain).when(domainService).getDomain(DOMAIN);

        doReturn("/gdc/c4/user/" + USER1).when(c4Domain).getOwner();
    }

    @Test
    public void testListAuditEvents() {
        ResponseEntity<AuditEventsDTO> result = testRestTemplate.exchange(ADMIN_URI, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2),  EntityDTOIdMatcher.hasSameIdAs(event3), EntityDTOIdMatcher.hasSameIdAs(event4), EntityDTOIdMatcher.hasSameIdAs(event5)));
    }

    @Test
    public void testListAuditEventsMultiplePages() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(2);
        String firstPageUri = requestParameters.getPageUri(UriComponentsBuilder.fromUriString(ADMIN_URI)).toString();

        ResponseEntity<AuditEventsDTO> firstPage = testRestTemplate.exchange(firstPageUri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(firstPage.getBody(), containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2)));
        String secondPageUri = firstPage.getBody().getPaging().getNextUri();
        assertThat(secondPageUri, notNullValue());

        ResponseEntity<AuditEventsDTO> secondPage = testRestTemplate.exchange(secondPageUri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(secondPage.getBody(), containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event3), EntityDTOIdMatcher.hasSameIdAs(event4)));

        String thirdPageUri = secondPage.getBody().getPaging().getNextUri();
        assertThat(thirdPageUri, notNullValue());

        ResponseEntity<AuditEventsDTO> thirdPage = testRestTemplate.exchange(thirdPageUri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(thirdPage, is(notNullValue()));
        assertThat(thirdPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(thirdPage.getBody(), Matchers.contains(EntityDTOIdMatcher.hasSameIdAs(event5)));

        String fourthPageUri = thirdPage.getBody().getPaging().getNextUri();
        assertThat(fourthPageUri, nullValue());
    }

    @Test
    public void testListAuditEventsNotAdmin() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-GDC-PUBLIC-USER-ID", USER2);
        HttpEntity<Object> objectHttpEntity = new HttpEntity<>(headers);

        ResponseEntity<ErrorStructure> result = testRestTemplate.exchange(ADMIN_URI, HttpMethod.GET, objectHttpEntity, ErrorStructure.class);

        assertThat(result.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testListAuditEventsInvalidOffset() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(2);
        requestParameters.setOffset("incorrect offset");
        String uri = requestParameters.getPageUri(UriComponentsBuilder.fromUriString(ADMIN_URI)).toString();

        ResponseEntity<ErrorStructure> result = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(), ErrorStructure.class);

        assertThat(result.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testListAuditEventsForUser() {
        ResponseEntity<AuditEventsDTO> result = testRestTemplate.exchange(USER_URI, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event3), EntityDTOIdMatcher.hasSameIdAs(event4)));
    }

    @Test
    public void testListAuditEventsForUserMultiplePages() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(1);
        String firstPageUri = requestParameters.getPageUri(UriComponentsBuilder.fromUriString(USER_URI)).toString();

        ResponseEntity<AuditEventsDTO> firstPage = testRestTemplate.exchange(firstPageUri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(firstPage.getBody(), Matchers.contains(EntityDTOIdMatcher.hasSameIdAs(event1)));
        String secondPageUri = firstPage.getBody().getPaging().getNextUri();
        assertThat(secondPageUri, notNullValue());

        ResponseEntity<AuditEventsDTO> secondPage = testRestTemplate.exchange(secondPageUri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(secondPage.getBody(), Matchers.contains(EntityDTOIdMatcher.hasSameIdAs(event3)));

        String thirdPageUri = secondPage.getBody().getPaging().getNextUri();
        assertThat(thirdPageUri, notNullValue());

        ResponseEntity<AuditEventsDTO> thirdPage = testRestTemplate.exchange(thirdPageUri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(thirdPage, is(notNullValue()));
        assertThat(thirdPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(thirdPage.getBody(), Matchers.contains(EntityDTOIdMatcher.hasSameIdAs(event4)));

        String fourthPageUri = thirdPage.getBody().getPaging().getNextUri();
        assertThat(fourthPageUri, nullValue());
    }

    @Test
    public void testListAuditEventsWithTimeIntervalFrom() {
        String uri = UriComponentsBuilder.fromUriString(ADMIN_URI)
                                         .query("from=" + TIME_2000)
                                         .build()
                                         .toString();

        ResponseEntity<AuditEventsDTO> result = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event4), EntityDTOIdMatcher.hasSameIdAs(event5)));
    }

    @Test
    public void testListAuditEventsWithTimeIntervalTo() {
        String uri = UriComponentsBuilder.fromUriString(ADMIN_URI)
                                         .query("to=" + TIME_2000)
                                         .build()
                                         .toString();

        ResponseEntity<AuditEventsDTO> result = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2), EntityDTOIdMatcher.hasSameIdAs(event3)));
    }

    @Test
    public void testListAuditEventsWithTimeIntervalFromAndTo() {
        String uri = UriComponentsBuilder.fromUriString(ADMIN_URI)
                                         .query("from=" + TIME_1995)
                                         .query("to=" + TIME_2000)
                                         .build()
                                         .toString();

        ResponseEntity<AuditEventsDTO> result = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event2), EntityDTOIdMatcher.hasSameIdAs(event3)));
    }

    @Test
    public void testListAuditEventsForUserWithTimeIntervalFrom() {
        String uri = UriComponentsBuilder.fromUriString(USER_URI)
                                         .query("from=" + TIME_1995)
                                         .build()
                                         .toString();

        ResponseEntity<AuditEventsDTO> result = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event3), EntityDTOIdMatcher.hasSameIdAs(event4)));
    }

    @Test
    public void testListAuditEventsForUserWithTimeIntervalTo() {
        String uri = UriComponentsBuilder.fromUriString(USER_URI)
                                         .query("to=" + TIME_1995)
                                         .build()
                                         .toString();

        ResponseEntity<AuditEventsDTO> result = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), contains(EntityDTOIdMatcher.hasSameIdAs(event1)));
    }

    @Test
    public void testListAuditEventsForUserWithTimeIntervalFromAndTo() {
        String uri = UriComponentsBuilder.fromUriString(USER_URI)
                                         .query("from=" + TIME_1990)
                                         .query("to=" + TIME_2000)
                                         .build()
                                         .toString();

        ResponseEntity<AuditEventsDTO> result = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), contains(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event3)));
    }

    @Test
    public void testListAuditEventsMultiplePagesWithTimeIntervalFromAndTo() {
        String uri = UriComponentsBuilder.fromUriString(ADMIN_URI)
                                         .query("from=" + TIME_1990)
                                         .query("to=" + TIME_2000)
                                         .build()
                                         .toString();

        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(2);
        String firstPageUri = requestParameters.getPageUri(UriComponentsBuilder.fromUriString(uri)).toString();

        ResponseEntity<AuditEventsDTO> firstPage = testRestTemplate.exchange(firstPageUri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(firstPage.getBody(), containsInAnyOrder(EntityDTOIdMatcher.hasSameIdAs(event1), EntityDTOIdMatcher.hasSameIdAs(event2)));

        String secondPageUri = firstPage.getBody().getPaging().getNextUri();
        assertThat(secondPageUri, is(notNullValue()));

        ResponseEntity<AuditEventsDTO> secondPage = testRestTemplate.exchange(secondPageUri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(secondPage.getBody(), contains(EntityDTOIdMatcher.hasSameIdAs(event3)));
    }

    private HttpEntity<AuditEventsDTO> requestWithGdcHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-GDC-PUBLIC-USER-ID", USER1);
        return new HttpEntity<>(headers);
    }
}