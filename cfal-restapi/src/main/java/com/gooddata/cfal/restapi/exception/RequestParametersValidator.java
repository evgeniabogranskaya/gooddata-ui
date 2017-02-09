/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.exception;

import com.gooddata.cfal.restapi.dto.RequestParameters;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for {@link RequestParameters}
 */
@Component
public class RequestParametersValidator implements Validator {

    private static String ERROR_CODE = "requestParameters.invalid";

    public static String INVALID_OFFSET_MSG = "invalid offset";

    public static String OFFSET_AND_FROM_SPECIFIED_MSG = "offset and time interval param \"from\" cannot be specified at once";

    public static String INVALID_TIME_INTERVAL_MSG = "\"to\" must be after \"before\"";

    public static String NOT_POSITIVE_LIMIT_MSG = "limit parameter must be positive number";


    @Override
    public boolean supports(final Class<?> aClass) {
        return aClass.isAssignableFrom(RequestParameters.class);
    }

    @Override
    public void validate(final Object o, final Errors errors) {
        final RequestParameters requestParameters = (RequestParameters) o;
        if(requestParameters.getOffset() != null) {
            if (!ObjectId.isValid(requestParameters.getOffset())) {
                errors.rejectValue("offset", ERROR_CODE, INVALID_OFFSET_MSG);
            }
        }

        if (requestParameters.getOffset() != null && requestParameters.getFrom() != null) {
            errors.rejectValue("offset", ERROR_CODE, OFFSET_AND_FROM_SPECIFIED_MSG);
        }

        if (requestParameters.getFrom() != null && requestParameters.getTo() != null) {
            if (!requestParameters.getFrom().isBefore(requestParameters.getTo())) {
                errors.rejectValue("from", ERROR_CODE, INVALID_TIME_INTERVAL_MSG);
            }
        }

        if(requestParameters.getLimit() <= 0 ) {
            errors.rejectValue("limit", ERROR_CODE, NOT_POSITIVE_LIMIT_MSG);
        }
    }
}
