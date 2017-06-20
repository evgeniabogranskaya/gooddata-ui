/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.springframework.web.util.UriComponentsBuilder;

public class RequestParametersTest {

    private static final DateTime FROM = new DateTime();
    private static final DateTime TO = new DateTime();
    private static final Integer LIMIT = 10;
    private static final String OFFSET = new ObjectId().toString();
    public static final String EVENT_TYPE = "STANDARD_LOGIN";

    @Test
    public void testCopy() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setFrom(FROM);
        requestParameters.setTo(TO);
        requestParameters.setLimit(LIMIT);
        requestParameters.setOffset(OFFSET);
        requestParameters.setType(EVENT_TYPE);

        RequestParameters copy = RequestParameters.copy(requestParameters);

        assertThat(requestParameters, is(copy));
    }

    @Test(expected = NullPointerException.class)
    public void testCopyNull() {
        RequestParameters.copy(null);
    }

    @Test
    public void testWithIncrementedLimit() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setFrom(FROM);
        requestParameters.setTo(TO);
        requestParameters.setLimit(LIMIT);
        requestParameters.setOffset(OFFSET);
        requestParameters.setType(EVENT_TYPE);

        RequestParameters result = requestParameters.withIncrementedLimit();

        assertThat(result.getFrom(), is(FROM));
        assertThat(result.getTo(), is(TO));
        assertThat(result.getSanitizedLimit(), is(LIMIT+1));
        assertThat(result.getOffset(), is(OFFSET));
        assertThat(result.getType(), is(EVENT_TYPE));
    }

    @Test
    public void testUpdateWithAllRequestParameters() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setFrom(FROM);
        requestParameters.setTo(TO);
        requestParameters.setLimit(LIMIT);
        requestParameters.setOffset(OFFSET);
        requestParameters.setType(EVENT_TYPE);

        UriComponentsBuilder result = requestParameters.updateWithPageParams(UriComponentsBuilder.newInstance());

        assertThat(result.build().toUriString(), is(String.format("?offset=%s&limit=%d&from=%s&to=%s&type=%s",
                OFFSET, LIMIT, FROM.toDateTime(DateTimeZone.UTC), TO.toDateTime(DateTimeZone.UTC), EVENT_TYPE)));
    }

    @Test
    public void testUpdateWithOnlyPagingRequestParameters() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setLimit(LIMIT);
        requestParameters.setOffset(OFFSET);

        UriComponentsBuilder result = requestParameters.updateWithPageParams(UriComponentsBuilder.newInstance());

        assertThat(result.build().toUriString(), is("?offset=" + OFFSET + "&limit=" + LIMIT));
    }

    @Test
    public void testUpdateWithOnlyTimeIntervalRequestParameters() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setFrom(FROM);
        requestParameters.setTo(TO);

        UriComponentsBuilder result = requestParameters.updateWithPageParams(UriComponentsBuilder.newInstance());

        assertThat(result.build().toUriString(), is("?limit=" + RequestParameters.DEFAULT_LIMIT + "&from=" + FROM.toDateTime(DateTimeZone.UTC) + "&to=" + TO.toDateTime(DateTimeZone.UTC)));
    }
}
