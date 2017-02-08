/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.junit.Test;

public class RequestParametersTest {

    private static final DateTime FROM = new DateTime();
    private static final DateTime TO = new DateTime();
    private static final Integer LIMIT = 10;
    private static final String OFFSET = new ObjectId().toString();
    
    @Test
    public void testCopy() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setFrom(FROM);
        requestParameters.setTo(TO);
        requestParameters.setLimit(LIMIT);
        requestParameters.setOffset(OFFSET);

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

        RequestParameters result = requestParameters.withIncrementedLimit();

        assertThat(result.getFrom(), is(FROM));
        assertThat(result.getTo(), is(TO));
        assertThat(result.getSanitizedLimit(), is(LIMIT+1));
        assertThat(result.getOffset(), is(OFFSET));
    }
}
