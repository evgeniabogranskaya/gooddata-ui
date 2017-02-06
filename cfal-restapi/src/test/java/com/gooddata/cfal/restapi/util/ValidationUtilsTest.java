/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.util;

import com.gooddata.cfal.restapi.dto.RequestParameters;
import com.gooddata.cfal.restapi.exception.InvalidOffsetException;
import com.gooddata.cfal.restapi.exception.InvalidTimeIntervalException;
import com.gooddata.cfal.restapi.exception.OffsetAndFromSpecifiedException;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.junit.Test;

public class ValidationUtilsTest {

    private static String OFFSET = new ObjectId().toString();
    private static DateTime TIME = new DateTime();


    @Test(expected = NullPointerException.class)
    public void testValidateNull() {
        ValidationUtils.validate(null);
    }

    @Test(expected = OffsetAndFromSpecifiedException.class)
    public void testValidateFromAndOffsetSpecified() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setOffset(OFFSET);
        requestParameters.setFrom(TIME);

        ValidationUtils.validate(requestParameters);
    }

    @Test(expected = InvalidTimeIntervalException.class)
    public void testValidateInvalidTimeInterval() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setFrom(TIME);
        requestParameters.setTo(TIME.minusDays(2));

        ValidationUtils.validate(requestParameters);
    }

    @Test
    public void testValidateSuccess() {
        ValidationUtils.validate(new RequestParameters());
    }

    @Test(expected = InvalidOffsetException.class)
    public void testInvalidOffset() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setOffset("test");

        ValidationUtils.validate(requestParameters);
    }

    @Test
    public void testValidOffset() {
        RequestParameters requestParameters = new RequestParameters();
        requestParameters.setOffset(OFFSET);

        ValidationUtils.validate(requestParameters);
    }
}