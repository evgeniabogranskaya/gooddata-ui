/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.exception;

import com.gooddata.exception.GdcErrorCode;
import com.gooddata.exception.GdcRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * In case of offset cannot be converted to {@link org.bson.types.ObjectId}
 */
@GdcErrorCode(id = "gdc.auditlog.invalid_offset", description = "offset in request is invalid")
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidOffsetException extends GdcRuntimeException {
    public InvalidOffsetException(final String message) {
        super(message);
    }

    public InvalidOffsetException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
