/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.exception;

/**
 * Exception when domain is not found
 */
@GdcErrorCode(id = "gdc.auditlog.domain_not_found", description = "domain was not found, probably does not exist")
public class DomainNotFoundException extends GdcRuntimeException {
    public DomainNotFoundException(final String message) {
        super(message);
    }

    public DomainNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
