/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.util;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public abstract class DateUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd").withZone(DateTimeZone.UTC);

    /**
     * Converts string date in format yyyy-MM-dd to DateTime instance in zone UTC
     *
     * @param date string yyyy-MM-dd
     * @return DateTime instance corresponding to given string
     */
    public static DateTime date(String date) {
        return FORMATTER.parseDateTime(date);
    }

    /**
     * Converts datetime to ObjectId instance
     * 
     * @param time time to be converted
     * @return ObjectId instance corresponding to given time
     */
    public static ObjectId convertDateTimeToObjectId(final DateTime time) {
        return new ObjectId(time.toDate());
    }
}
