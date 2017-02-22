/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.util;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.restapi.dto.AuditEventsDTO;
import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.cfal.restapi.model.AuditEvent;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

public class ConversionUtilsTest {

    private static final String BASE_URI = "uri";

    private static final ObjectId ID = new ObjectId();
    private static final String DOMAIN = "domain";
    private static final String USER_ID = "userId";
    private static final DateTime TIME = new DateTime();

    @Test
    public void testCreateAuditEventDTO() {
        AuditEventDTO auditEventDTO = ConversionUtils.createAuditEventDTO(new AuditEvent(ID, DOMAIN, USER_ID, TIME));

        assertThat(auditEventDTO.getId(), is(ID.toString()));
        assertThat(auditEventDTO.getDomain(), is(DOMAIN));
        assertThat(auditEventDTO.getUserId(), is(USER_ID));
        assertThat(auditEventDTO.getRealTimeOccurrence(), is(TIME));
        assertThat(auditEventDTO.getTimestamp(), is(new DateTime(ID.getDate(), DateTimeZone.UTC)));
    }

    @Test(expected = NullPointerException.class)
    public void testCreateAuditEventDTOnullValue() {
        ConversionUtils.createAuditEventDTO(null);
    }

    @Test
    public void testCreateAuditEventsDTO() {
        RequestParameters requestParameters = new RequestParameters();
        AuditEventsDTO auditEventsDTO = ConversionUtils.createAuditEventsDTO(
                BASE_URI, Collections.singletonList(new AuditEvent(ID, DOMAIN, USER_ID, TIME)), requestParameters);

        assertThat(auditEventsDTO, hasSize(1));
        assertThat(auditEventsDTO.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testCreateAuditEventsDTOWithTimeParameterTo() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setTo(TIME);
        AuditEventsDTO auditEventsDTO = ConversionUtils.createAuditEventsDTO(
                BASE_URI, Collections.singletonList(new AuditEvent(ID, DOMAIN, USER_ID, TIME)), requestParameters);

        assertThat(auditEventsDTO, hasSize(1));
        assertThat(auditEventsDTO.getPaging().getNextUri(), is(nullValue()));
    }

    @Test(expected = NullPointerException.class)
    public void testCreateAuditEventsDTOnullList() {
        ConversionUtils.createAuditEventsDTO(BASE_URI, null, new RequestParameters());
    }

    @Test(expected = NullPointerException.class)
    public void testCreateAuditEventsDTOnullUri() {
        ConversionUtils.createAuditEventsDTO(null, new ArrayList<>(), new RequestParameters());
    }

    @Test(expected = NullPointerException.class)
    public void testCreateAuditEventsDTOnullRequestParameters() {
        ConversionUtils.createAuditEventsDTO(BASE_URI, new ArrayList<>(), null);
    }

    @Test
    public void testCreateAuditEventsDTOemptyList() {
        AuditEventsDTO auditEventsDTO = ConversionUtils.createAuditEventsDTO(BASE_URI, Collections.emptyList(), new RequestParameters());

        assertThat(auditEventsDTO.getPaging().getNextUri(), is(nullValue()));
    }

    @Test
    public void testCreateAuditEventsDTOListHasMoreElementsThanLimit() {
        AuditEvent event = new AuditEvent(ID, DOMAIN, USER_ID, TIME);

        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(3);

        AuditEventsDTO auditEventsDTO = ConversionUtils.createAuditEventsDTO(BASE_URI, asList(event, event, event, event), requestParameters);

        assertThat(auditEventsDTO, hasSize(3));
        assertThat(auditEventsDTO.getPaging().getNextUri(), is(notNullValue()));
    }

    @Test
    public void testCreateAuditEventsDTOListHasExactlyElementsOfLimit() {
        AuditEvent event = new AuditEvent(ID, DOMAIN, USER_ID, TIME);

        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(3);

        AuditEventsDTO auditEventsDTO = ConversionUtils.createAuditEventsDTO(BASE_URI, asList(event, event, event), requestParameters);

        assertThat(auditEventsDTO, hasSize(3));
        assertThat(auditEventsDTO.getPaging().getNextUri(), is(nullValue()));
    }
}
