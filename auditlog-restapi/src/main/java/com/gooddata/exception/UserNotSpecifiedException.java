/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception when user is not specified in request
 */
@GdcErrorCode(id = "gdc.auditlog.user.not_specified", description = "user ID was not specified in request")
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class UserNotSpecifiedException extends GdcRuntimeException {
    public UserNotSpecifiedException(final String message) {
        super(message);
    }

    public UserNotSpecifiedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
