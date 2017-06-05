/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.exception;

import com.gooddata.exception.GdcRuntimeException;

/**
 * Exception when domain is not found
 */
public class DomainNotFoundException extends GdcRuntimeException {
    public DomainNotFoundException(final String message) {
        super(message);
    }

    public DomainNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
