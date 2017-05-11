/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.exception;

import com.gooddata.exception.GdcErrorCode;
import com.gooddata.exception.GdcRuntimeException;
import com.gooddata.exception.annotation.LoggingLevel;
import com.gooddata.exception.annotation.LoggingSettings;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception when user is accessing user API of user he is not authorized to see events
 */
@GdcErrorCode(id = "gdc.auditlog.user.not_authorized", description = "user is not authorized to access this resource")
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
@LoggingSettings(level = LoggingLevel.INFO)
public class UserNotAuthorizedException extends GdcRuntimeException {
    public UserNotAuthorizedException(final String msg) {
        super(msg);
    }

    public UserNotAuthorizedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
