/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.core.convert.converter.Converter;

/**
 * Converts String to DateTime with UTC timezone
 */
public class StringToUTCDateTimeConverter implements Converter<String, DateTime> {

    @Override
    public DateTime convert(final String s) {
        if(StringUtils.isBlank(s)) {
            return null;
        }
        return DateTime.parse(s, ISODateTimeFormat.dateTime().withZoneUTC());
    }
}
