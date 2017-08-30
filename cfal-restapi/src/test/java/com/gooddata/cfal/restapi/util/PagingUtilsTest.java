/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.gooddata.auditevent.AuditEventPageRequest;
import com.gooddata.collections.Paging;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.junit.Test;

public class PagingUtilsTest {

    private static final String BASE_URI = "uri";
    private static final String NEXT_OFFSET = new ObjectId().toString();
    private static final DateTime TO = new DateTime();

    @Test
    public void testCreatePaging() {
        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        Paging paging = PagingUtils.createPaging(BASE_URI, requestParameters, NEXT_OFFSET);

        assertThat(paging.getNextUri(), is(BASE_URI + "?offset=" + NEXT_OFFSET + "&limit=" + requestParameters.getSanitizedLimit()));
    }

    @Test
    public void testCreatePagingWithToParameter() {
        AuditEventPageRequest requestParameters = new AuditEventPageRequest();
        requestParameters.setTo(TO);
        Paging paging = PagingUtils.createPaging(BASE_URI, requestParameters, NEXT_OFFSET);

        assertThat(paging.getNextUri(),
                is(BASE_URI + "?to=" + TO + "&offset=" + NEXT_OFFSET + "&limit=" + requestParameters.getSanitizedLimit()));
    }

    @Test
    public void testCreatePagingWithNullOffset() {
        Paging paging = PagingUtils.createPaging(BASE_URI, new AuditEventPageRequest(), null);

        assertThat(paging.getNextUri(),
                is(nullValue()));
    }
}
