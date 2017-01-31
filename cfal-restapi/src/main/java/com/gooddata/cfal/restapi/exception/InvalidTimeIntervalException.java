/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.exception;

import com.gooddata.exception.GdcErrorCode;
import com.gooddata.exception.GdcRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@GdcErrorCode(id = "gdc.auditlog.invalid_time_interval", description = "time interval is invalid")
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidTimeIntervalException extends GdcRuntimeException {
    public InvalidTimeIntervalException(final String message) {
        super(message);
    }

    public InvalidTimeIntervalException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
