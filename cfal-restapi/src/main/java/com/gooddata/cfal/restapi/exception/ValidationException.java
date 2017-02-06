/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.exception;

import com.gooddata.exception.GdcErrorCode;
import com.gooddata.exception.GdcRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@GdcErrorCode(id = "gdc.auditlog.validation_failed", description = "validation failed")
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ValidationException extends GdcRuntimeException {
    public ValidationException(final String message) {
        super(message);
    }

    public ValidationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
