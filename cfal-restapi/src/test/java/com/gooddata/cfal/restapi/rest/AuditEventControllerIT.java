/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.rest;

import com.gooddata.c4.domain.C4Domain;
import com.gooddata.c4.domain.DomainService;
import com.gooddata.c4.setting.C4SettingEntry;
import com.gooddata.c4.user.C4User;
import com.gooddata.c4.user.UserService;
import com.gooddata.auditevent.AuditEvents;
import com.gooddata.auditevent.AuditEventPageRequest;
import com.gooddata.cfal.restapi.model.AuditEventEntity;
import com.gooddata.cfal.restapi.repository.AuditLogEventRepository;
import com.gooddata.commons.monitoring.metrics.boot.EnableMonitoring;
import com.gooddata.exception.servlet.ErrorStructure;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import static com.gooddata.auditevent.AuditEvent.ADMIN_URI_TEMPLATE;
import static com.gooddata.auditevent.AuditEvent.USER_URI_TEMPLATE;
import static com.gooddata.cfal.restapi.util.DateUtils.convertDateTimeToObjectId;
import static com.gooddata.cfal.restapi.util.DateUtils.date;
import static com.gooddata.cfal.restapi.util.EntityDTOIdMatcher.hasSameIdAs;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class AuditEventControllerIT {

    private static final String DOMAIN = RandomStringUtils.randomAlphabetic(10);

    private static final String USER1_ID = RandomStringUtils.randomAlphabetic(10);
    private static final String USER2_ID = RandomStringUtils.randomAlphabetic(10);

    private static final String USER1_LOGIN = "bear@gooddata.com";
    private static final String USER2_LOGIN = "jane@gooddata.com";

    private static final DateTime TIME_2000 = date("2000-03-09");
    private static final DateTime TIME_1990 = date("1990-01-01");
    private static final DateTime TIME_1995 = date("1995-01-01");

    private static final String IP = "128.1.2.3";
    private static final String IP2 = "129.1.2.3";
    private static final String LOCAL_IP = "127.0.0.1";
    private static final boolean SUCCESS = true;
    private static final String TYPE = "login";
    private static final String TYPE2 = "logout";
    private static final Map<String, String> EMPTY_PARAMS = new HashMap<>();
    private static final Map<String, String> EMPTY_LINKS = new HashMap<>();

    @Value("${cfal.restapi.security.user.name}")
    private String name;

    @Value("${cfal.restapi.security.user.password}")
    private String password;

    @MockBean
    private UserService userService;

    @MockBean
    private DomainService domainService;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private AuditLogEventRepository auditLogEventRepository;

    private AuditEventEntity event1 = new AuditEventEntity(convertDateTimeToObjectId(date("1993-03-09")), DOMAIN, USER1_LOGIN, date("1993-03-09"), IP, SUCCESS, TYPE, EMPTY_PARAMS, EMPTY_LINKS);
    private AuditEventEntity event2 = new AuditEventEntity(convertDateTimeToObjectId(date("1995-03-09")), DOMAIN, USER2_LOGIN, date("1995-03-09"), IP2, SUCCESS, TYPE, EMPTY_PARAMS, EMPTY_LINKS);
    private AuditEventEntity event3 = new AuditEventEntity(convertDateTimeToObjectId(date("1996-03-09")), DOMAIN, USER1_LOGIN, date("1996-03-09"), IP, SUCCESS, TYPE, EMPTY_PARAMS, EMPTY_LINKS);
    private AuditEventEntity event4 = new AuditEventEntity(convertDateTimeToObjectId(date("2001-03-09")), DOMAIN, USER1_LOGIN, date("2001-03-09"), IP2, SUCCESS, TYPE, EMPTY_PARAMS, EMPTY_LINKS);
    private AuditEventEntity event5 = new AuditEventEntity(convertDateTimeToObjectId(date("2016-03-09")), DOMAIN, USER2_LOGIN, date("2016-03-09"), IP, SUCCESS, TYPE, EMPTY_PARAMS, EMPTY_LINKS);
    private AuditEventEntity event6 = new AuditEventEntity(convertDateTimeToObjectId(date("2016-03-09")), DOMAIN, USER2_LOGIN, date("2016-03-09"), IP2, SUCCESS, TYPE2, EMPTY_PARAMS, EMPTY_LINKS);

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
        auditLogEventRepository.save(event6);

        c4User1 = mock(C4User.class);
        c4User2 = mock(C4User.class);

        doReturn("/gdc/c4/domain/" + DOMAIN).when(c4User1).getDomainUri();
        doReturn("/gdc/c4/domain/" + DOMAIN).when(c4User2).getDomainUri();

        doReturn(USER1_LOGIN).when(c4User1).getLogin();
        doReturn(USER2_LOGIN).when(c4User2).getLogin();

        doReturn(c4User1).when(userService).getUser(USER1_ID);
        doReturn(c4User2).when(userService).getUser(USER2_ID);

        doReturn(new C4SettingEntry("cfal", "true")).when(userService).getSetting(anyString(), eq("cfal"));

        c4Domain = mock(C4Domain.class);

        doReturn(c4Domain).when(domainService).getDomain(DOMAIN);

        doReturn("/gdc/c4/user/" + USER1_ID).when(c4Domain).getOwner();
    }

    @Test
    public void testListAuditEvents() {
        ResponseEntity<AuditEvents> result = testRestTemplate.exchange(adminUri(), HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event2), hasSameIdAs(event3),
                hasSameIdAs(event4), hasSameIdAs(event5), hasSameIdAs(event6)));
        assertThat(result.getBody().stream().map(e -> e.getUserIp()).collect(Collectors.toList()), contains(LOCAL_IP, IP2, LOCAL_IP, IP2, LOCAL_IP, IP2));
    }

    @Test
    public void testListAuditEventsMultiplePages() {
        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setLimit(2);

        String firstPageUri = createUriWithParams(requestParameters, adminUri());

        ResponseEntity<AuditEvents> firstPage = testRestTemplate.exchange(firstPageUri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(firstPage.getBody(), containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event2)));
        assertThat(firstPage.getBody().stream().map(e -> e.getUserIp()).collect(Collectors.toList()), contains(LOCAL_IP, IP2));
        String secondPageUri = firstPage.getBody().getPaging().getNextUri();
        assertThat(secondPageUri, notNullValue());

        ResponseEntity<AuditEvents> secondPage = testRestTemplate.exchange(secondPageUri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(secondPage.getBody(), containsInAnyOrder(hasSameIdAs(event3), hasSameIdAs(event4)));
        assertThat(secondPage.getBody().stream().map(e -> e.getUserIp()).collect(Collectors.toList()), contains(LOCAL_IP, IP2));

        String thirdPageUri = secondPage.getBody().getPaging().getNextUri();
        assertThat(thirdPageUri, notNullValue());

        ResponseEntity<AuditEvents> thirdPage = testRestTemplate.exchange(thirdPageUri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(thirdPage, is(notNullValue()));
        assertThat(thirdPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(thirdPage.getBody(), Matchers.containsInAnyOrder(hasSameIdAs(event5), hasSameIdAs(event6)));
        assertThat(thirdPage.getBody().stream().map(e -> e.getUserIp()).collect(Collectors.toList()), contains(LOCAL_IP, IP2));

        String fourthPageUri = thirdPage.getBody().getPaging().getNextUri();
        assertThat(fourthPageUri, nullValue());
    }

    @Test
    public void testListAuditEventsNotAdmin() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-GDC-PUBLIC-USER-ID", USER2_ID);
        HttpEntity<Object> objectHttpEntity = new HttpEntity<>(headers);

        ResponseEntity<ErrorStructure> result = testRestTemplate.exchange(adminUri(), HttpMethod.GET, objectHttpEntity, ErrorStructure.class);

        assertThat(result.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testListAuditEventsInvalidOffset() {
        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setLimit(2);
        requestParameters.setOffset("incorrect offset");

        String uri = createUriWithParams(requestParameters, adminUri());

        ResponseEntity<ErrorStructure> result = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), ErrorStructure.class);

        assertThat(result.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testListAuditEventsInvalidType() {
        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setType("_x");

        String uri = createUriWithParams(requestParameters, adminUri());

        ResponseEntity<ErrorStructure> result = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), ErrorStructure.class);

        assertThat(result.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testListAuditEventsForUserInvalidType() {
        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setType("_x");

        String uri = createUriWithParams(requestParameters, userUri(USER1_ID));

        ResponseEntity<ErrorStructure> result = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), ErrorStructure.class);

        assertThat(result.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testListAuditEventsForUser() {
        ResponseEntity<AuditEvents> result = testRestTemplate.exchange(userUri(USER1_ID), HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event3), hasSameIdAs(event4)));
    }

    @Test
    public void testListAuditEventsForUserMultiplePages() {
        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setLimit(1);

        String firstPageUri = createUriWithParams(requestParameters, userUri(USER1_ID));

        ResponseEntity<AuditEvents> firstPage = testRestTemplate.exchange(firstPageUri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(firstPage.getBody(), Matchers.contains(hasSameIdAs(event1)));
        String secondPageUri = firstPage.getBody().getPaging().getNextUri();
        assertThat(secondPageUri, notNullValue());

        ResponseEntity<AuditEvents> secondPage = testRestTemplate.exchange(secondPageUri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(secondPage.getBody(), Matchers.contains(hasSameIdAs(event3)));

        String thirdPageUri = secondPage.getBody().getPaging().getNextUri();
        assertThat(thirdPageUri, notNullValue());

        ResponseEntity<AuditEvents> thirdPage = testRestTemplate.exchange(thirdPageUri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(thirdPage, is(notNullValue()));
        assertThat(thirdPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(thirdPage.getBody(), Matchers.contains(hasSameIdAs(event4)));

        String fourthPageUri = thirdPage.getBody().getPaging().getNextUri();
        assertThat(fourthPageUri, nullValue());
    }

    @Test
    public void testListAuditEventsWithType() {
        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setType(TYPE2);

        String uri = createUriWithParams(requestParameters, adminUri());

        ResponseEntity<AuditEvents> result = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), contains(hasSameIdAs(event6)));
    }

    @Test
    public void testListAuditEventsForUserWithType() {
        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setType(TYPE2);

        String uri = createUriWithParams(requestParameters, userUri(USER2_ID));

        ResponseEntity<AuditEvents> result = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(USER2_ID), AuditEvents.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), contains(hasSameIdAs(event6)));
    }

    @Test
    public void testListAuditEventsWithTypeMultiplePages() {
        final AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setType(TYPE);
        requestParameters.setLimit(4);

        final String uri = createUriWithParams(requestParameters, adminUri());

        final ResponseEntity<AuditEvents> firstPage = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage.getBody(), contains(hasSameIdAs(event1), hasSameIdAs(event2), hasSameIdAs(event3), hasSameIdAs(event4)));

        final String secondPageUri = firstPage.getBody().getPaging().getNextUri();

        assertThat(secondPageUri, containsString("type=" + TYPE));

        final ResponseEntity<AuditEvents> secondPage = testRestTemplate.exchange(secondPageUri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage.getBody(), contains(hasSameIdAs(event5)));
    }

    @Test
    public void testListAuditEventsWithTimeIntervalFrom() {
        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setFrom(TIME_2000);

        String uri = createUriWithParams(requestParameters, adminUri());

        ResponseEntity<AuditEvents> result = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), containsInAnyOrder(hasSameIdAs(event4), hasSameIdAs(event5), hasSameIdAs(event6)));
    }

    @Test
    public void testListAuditEventsWithTimeIntervalTo() {
        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setTo(TIME_2000);

        String uri = createUriWithParams(requestParameters, adminUri());

        ResponseEntity<AuditEvents> result = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event2), hasSameIdAs(event3)));
    }

    @Test
    public void testListAuditEventsWithTimeIntervalFromAndTo() {
        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setFrom(TIME_1995);
        requestParameters.setTo(TIME_2000);

        String uri = createUriWithParams(requestParameters, adminUri());

        ResponseEntity<AuditEvents> result = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), containsInAnyOrder(hasSameIdAs(event2), hasSameIdAs(event3)));
    }

    @Test
    public void testListAuditEventsForUserWithTimeIntervalFrom() {
        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setFrom(TIME_1995);

        String uri = createUriWithParams(requestParameters, userUri(USER1_ID));

        ResponseEntity<AuditEvents> result = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), containsInAnyOrder(hasSameIdAs(event3), hasSameIdAs(event4)));
    }

    @Test
    public void testListAuditEventsForUserWithTimeIntervalTo() {
        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setTo(TIME_1995);

        String uri = createUriWithParams(requestParameters, userUri(USER1_ID));

        ResponseEntity<AuditEvents> result = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), contains(hasSameIdAs(event1)));
    }

    @Test
    public void testListAuditEventsForUserWithTimeIntervalFromAndTo() {
        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setFrom(TIME_1990);
        requestParameters.setTo(TIME_2000);

        String uri = createUriWithParams(requestParameters, userUri(USER1_ID));

        ResponseEntity<AuditEvents> result = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), contains(hasSameIdAs(event1), hasSameIdAs(event3)));
    }

    @Test
    public void testListAuditEventsMultiplePagesWithTimeIntervalFromAndTo() {
        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setFrom(TIME_1990);
        requestParameters.setTo(TIME_2000);
        requestParameters.setLimit(2);

        String firstPageUri = createUriWithParams(requestParameters, adminUri());

        ResponseEntity<AuditEvents> firstPage = testRestTemplate.exchange(firstPageUri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(firstPage.getBody(), containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event2)));

        String secondPageUri = firstPage.getBody().getPaging().getNextUri();
        assertThat(secondPageUri, is(notNullValue()));

        ResponseEntity<AuditEvents> secondPage = testRestTemplate.exchange(secondPageUri, HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(secondPage.getBody(), contains(hasSameIdAs(event3)));
    }

    @Test
    public void testUserAccessingUserAPIforDifferentUser() {
        ResponseEntity<ErrorStructure> result = testRestTemplate.exchange(userUri(USER1_ID), HttpMethod.GET, requestWithGdcHeader(USER2_ID), ErrorStructure.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testAdminAccessingUserAPIforDifferentUser() {
        ResponseEntity<AuditEvents> result = testRestTemplate.exchange(userUri(USER2_ID), HttpMethod.GET, requestWithGdcHeader(USER1_ID), AuditEvents.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), contains(hasSameIdAs(event2), hasSameIdAs(event5), hasSameIdAs(event6)));
    }

    @Test
    public void testUserAccessingUserAPI() {
        ResponseEntity<AuditEvents> result = testRestTemplate.exchange(userUri(USER2_ID), HttpMethod.GET, requestWithGdcHeader(USER2_ID), AuditEvents.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), contains(hasSameIdAs(event2), hasSameIdAs(event5), hasSameIdAs(event6)));
    }

    @Test
    public void testInfoEndpointAvailableWithoutAuth() {
        ResponseEntity<String> result = testRestTemplate.exchange("/actuator/info", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void testHealthEndpointIsAvailableWithAuth() throws Exception {
        ResponseEntity<String> result = testRestTemplate.exchange("/actuator/health", HttpMethod.GET, requestWithBasicAuth(), String.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(not(HttpStatus.UNAUTHORIZED)));
    }

    @Test
    public void testEnvEndpointIsNotAvailableWithoutAuth() {
        ResponseEntity<String> result = testRestTemplate.exchange("/actuator/env", HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testEnvEndpointIsAvailableWithAuth() throws Exception {
        ResponseEntity<String> result = testRestTemplate.exchange("/actuator/env", HttpMethod.GET, requestWithBasicAuth(), String.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(not(HttpStatus.UNAUTHORIZED)));
    }

    private HttpEntity<AuditEvents> requestWithGdcHeader(final String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-GDC-PUBLIC-USER-ID", userId);
        headers.add("Content-Type", "application/json");
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Object> requestWithBasicAuth() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + new String(Base64.encode((name + ":" + password).getBytes())));
        return new HttpEntity<>(headers);
    }

    private String adminUri() {
        return ADMIN_URI_TEMPLATE.expand(DOMAIN).toString();
    }

    private String userUri(final String userId) {
        return USER_URI_TEMPLATE.expand(userId).toString();
    }

    private String createUriWithParams(final AuditEventPageRequest requestParameters, final String uri) {
        return requestParameters.updateWithPageParams(UriComponentsBuilder.fromUriString(uri)).build().toUriString();
    }
}