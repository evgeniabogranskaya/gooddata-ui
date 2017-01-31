/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.exception;

import com.gooddata.exception.GdcErrorCode;
import com.gooddata.exception.GdcRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * When <i>offset</i> and <i>from</i> in time interval request are specified at once
 */
@GdcErrorCode(id = "gdc.auditlog.offset_and_from_specified", description = "offset and from parameter of time interval request cannot be specified at once")
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class OffsetAndFromSpecifiedException extends GdcRuntimeException {
    public OffsetAndFromSpecifiedException(final String message) {
        super(message);
    }

    public OffsetAndFromSpecifiedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
