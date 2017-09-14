/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.validation;

import com.gooddata.auditevent.AuditEventPageRequest;
import org.bson.types.ObjectId;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for {@link AuditEventPageRequest}
 */
public class AuditEventPageRequestValidator implements Validator {

    public static final String OFFSET_FIELD = "offset";

    public static final String FROM_FIELD = "from";

    public static final String LIMIT_FIELD = "limit";

    public static final String TYPE_FIELD = "type";

    public static final String ERROR_CODE_INVALID_OFFSET = "requestParameters.invalid_offset";

    public static final String ERROR_CODE_OFFSET_FROM_SPECIFIED = "requestParameters.offset_from_specified";

    public static final String ERROR_CODE_INVALID_TIME_INTERVAL = "requestParameters.invalid_time_interval";

    public static final String ERROR_CODE_NOT_POSITIVE_LIMIT = "requestParameters.not_positive_limit";

    public static final String ERROR_CODE_NOT_VALID_TYPE = "requestParameters.not_matching_eventtype";

    public static final String INVALID_OFFSET_MSG = "invalid offset";

    public static final String OFFSET_AND_FROM_SPECIFIED_MSG = "offset and time interval param \"from\" cannot be specified at once";

    public static final String INVALID_TIME_INTERVAL_MSG = "\"to\" must be after \"before\"";

    public static final String NOT_POSITIVE_LIMIT_MSG = "limit parameter must be positive number";

    private static final String TYPE_REGEXP = "^[a-zA-Z]([a-zA-Z_]*)$";

    public static final String CODE_NOT_VALID_TYPE_MSG = "\"type\" does not match regularExpression=" + TYPE_REGEXP;

    @Override
    public boolean supports(final Class<?> aClass) {
        return aClass.isAssignableFrom(AuditEventPageRequest.class);
    }

    @Override
    public void validate(final Object o, final Errors errors) {
        final AuditEventPageRequest requestParameters = (AuditEventPageRequest) o;
        if (requestParameters.getOffset() != null && !ObjectId.isValid(requestParameters.getOffset())) {
            errors.rejectValue(OFFSET_FIELD, ERROR_CODE_INVALID_OFFSET, INVALID_OFFSET_MSG);
        }

        if (requestParameters.getOffset() != null && requestParameters.getFrom() != null) {
            errors.rejectValue(OFFSET_FIELD, ERROR_CODE_OFFSET_FROM_SPECIFIED, OFFSET_AND_FROM_SPECIFIED_MSG);
        }

        if (requestParameters.getFrom() != null && requestParameters.getTo() != null &&
                !requestParameters.getFrom().isBefore(requestParameters.getTo())) {
            errors.rejectValue(FROM_FIELD, ERROR_CODE_INVALID_TIME_INTERVAL, INVALID_TIME_INTERVAL_MSG);
        }

        if (requestParameters.getType() != null && !requestParameters.getType().matches(TYPE_REGEXP)) {
            errors.rejectValue(TYPE_FIELD, ERROR_CODE_NOT_VALID_TYPE, CODE_NOT_VALID_TYPE_MSG);
        }

        if (requestParameters.getLimit() <= 0) {
            errors.rejectValue(LIMIT_FIELD, ERROR_CODE_NOT_POSITIVE_LIMIT, NOT_POSITIVE_LIMIT_MSG);
        }
    }
}
