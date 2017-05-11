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
 * Exception when user is not found
 */
@GdcErrorCode(id = "gdc.auditlog.user.not_found", description = "user was not found, probably does not exist")
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
@LoggingSettings(level = LoggingLevel.INFO)
public class UserNotFoundException extends GdcRuntimeException {
    public UserNotFoundException(final String message) {
        super(message);
    }

    public UserNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
