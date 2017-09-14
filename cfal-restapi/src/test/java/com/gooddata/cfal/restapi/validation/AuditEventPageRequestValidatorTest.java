/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.validation;

import static cz.geek.spring.test.ErrorsMatchers.hasFieldError;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.gooddata.auditevent.AuditEventPageRequest;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class AuditEventPageRequestValidatorTest {

    private final AuditEventPageRequestValidator validator = new AuditEventPageRequestValidator();

    private AuditEventPageRequest request;
    private Errors errors;

    @Before
    public void setUp() {
        request = new AuditEventPageRequest();
        errors = new BindException(request, "request");
    }

    @Test
    public void testSupportsRequestParameters() {
        assertThat(validator.supports(AuditEventPageRequest.class), is(true));
    }

    @Test
    public void testValidateInvalidOffset() {
        request.setOffset("test");

        validator.validate(request, errors);

        assertThat(errors, hasFieldError(AuditEventPageRequestValidator.OFFSET_FIELD, AuditEventPageRequestValidator.ERROR_CODE_INVALID_OFFSET));
    }

    @Test
    public void testValidateOffsetAndFromSpecified() {
        request.setOffset(new ObjectId().toString());
        request.setFrom(new DateTime());

        validator.validate(request, errors);

        assertThat(errors, hasFieldError(AuditEventPageRequestValidator.OFFSET_FIELD, AuditEventPageRequestValidator.ERROR_CODE_OFFSET_FROM_SPECIFIED));
    }

    @Test
    public void testValidateInvalidTimeInterval() {
        request.setFrom(new DateTime());
        request.setTo(new DateTime().minusDays(2));

        validator.validate(request, errors);

        assertThat(errors, hasFieldError(AuditEventPageRequestValidator.FROM_FIELD, AuditEventPageRequestValidator.ERROR_CODE_INVALID_TIME_INTERVAL));
    }

    @Test
    public void testValidateNegativeLimit() {
        request.setLimit(-1);

        validator.validate(request, errors);

        assertThat(errors, hasFieldError(AuditEventPageRequestValidator.LIMIT_FIELD, AuditEventPageRequestValidator.ERROR_CODE_NOT_POSITIVE_LIMIT));
    }

    @Test
    public void testValidateSuccess() {
        validator.validate(request, errors);

        assertThat(errors.getErrorCount(), is(0));
    }

    @Test
    public void testValidateEventType() {
        request.setType("_x");

        validator.validate(request, errors);

        assertThat(errors, hasFieldError(AuditEventPageRequestValidator.TYPE_FIELD, AuditEventPageRequestValidator.ERROR_CODE_NOT_VALID_TYPE));
    }
}
