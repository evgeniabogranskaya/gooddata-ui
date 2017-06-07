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
 * Exception when CFAL is not enabled via Feature flag
 */
@GdcErrorCode(id = "gdc.auditlog.not_enabled", description = "Feature flag is missing")
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
@LoggingSettings(level = LoggingLevel.INFO)
public class AuditLogNotEnabledException extends GdcRuntimeException {
    public AuditLogNotEnabledException(final String message) {
        super(message);
    }

    public AuditLogNotEnabledException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
