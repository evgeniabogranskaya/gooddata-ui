/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.util;

import static com.gooddata.cfal.restapi.util.DateUtils.date;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

public class StringToUTCDateTimeConverterTest {

    private StringToUTCDateTimeConverter stringToUTCDateTimeConverter;

    @Before
    public void setUp() {
        stringToUTCDateTimeConverter = new StringToUTCDateTimeConverter();
    }

    @Test
    public void convertUTCString() throws Exception {
        DateTime expectedTime = date("1993-03-09");
        DateTime converted = stringToUTCDateTimeConverter.convert(expectedTime.toString());

        assertThat(expectedTime, is(equalTo(converted)));
    }

    @Test
    public void convertDifferentZoneString() throws Exception {
        DateTime expectedTime = new DateTime(1993, 9, 3, 2, 0, DateTimeZone.forOffsetHours(2));
        DateTime converted = stringToUTCDateTimeConverter.convert(expectedTime.toString());

        assertThat(expectedTime.toDateTime(DateTimeZone.UTC), is(equalTo(converted)));
    }

    @Test
    public void convertNull() throws Exception {
        assertThat(stringToUTCDateTimeConverter.convert(null), is(nullValue()));
    }

    @Test
    public void convertEmptyString() throws Exception {
        assertThat(stringToUTCDateTimeConverter.convert(""), is(nullValue()));
    }
}
