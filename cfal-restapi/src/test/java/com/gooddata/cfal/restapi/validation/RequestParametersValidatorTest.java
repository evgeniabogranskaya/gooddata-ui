/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.validation;

import static cz.geek.spring.test.ErrorsMatchers.hasFieldError;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.gooddata.cfal.restapi.dto.RequestParameters;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class RequestParametersValidatorTest {

    private final RequestParametersValidator requestParametersValidator = new RequestParametersValidator();

    private RequestParameters requestParameters;
    private Errors errors;

    @Before
    public void setUp() {
        requestParameters = new RequestParameters();
        errors = new BindException(requestParameters, "requestParameters");
    }

    @Test
    public void testSupportsRequestParameters() {
        assertThat(requestParametersValidator.supports(RequestParameters.class), is(true));
    }

    @Test
    public void testValidateInvalidOffset() {
        requestParameters.setOffset("test");

        requestParametersValidator.validate(requestParameters, errors);

        assertThat(errors, hasFieldError(RequestParametersValidator.OFFSET_FIELD, RequestParametersValidator.ERROR_CODE_INVALID_OFFSET));
    }

    @Test
    public void testValidateOffsetAndFromSpecified() {
        requestParameters.setOffset(new ObjectId().toString());
        requestParameters.setFrom(new DateTime());

        requestParametersValidator.validate(requestParameters, errors);

        assertThat(errors, hasFieldError(RequestParametersValidator.OFFSET_FIELD, RequestParametersValidator.ERROR_CODE_OFFSET_FROM_SPECIFIED));
    }

    @Test
    public void testValidateInvalidTimeInterval() {
        requestParameters.setFrom(new DateTime());
        requestParameters.setTo(new DateTime().minusDays(2));

        requestParametersValidator.validate(requestParameters, errors);

        assertThat(errors, hasFieldError(RequestParametersValidator.FROM_FIELD, RequestParametersValidator.ERROR_CODE_INVALID_TIME_INTERVAL));
    }

    @Test
    public void testValidateNegativeLimit() {
        requestParameters.setLimit(-1);

        requestParametersValidator.validate(requestParameters, errors);

        assertThat(errors, hasFieldError(RequestParametersValidator.LIMIT_FIELD, RequestParametersValidator.ERROR_CODE_NOT_POSITIVE_LIMIT));
    }

    @Test
    public void testValidateSuccess() {
        requestParametersValidator.validate(requestParameters, errors);

        assertThat(errors.getErrorCount(), is(0));
    }
}
