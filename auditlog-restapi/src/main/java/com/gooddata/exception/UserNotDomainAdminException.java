/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception when user is not admin of domain
 */
@GdcErrorCode(id = "gdc.auditlog.user.not_domain_admin", description = "user is not admin of domain")
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UserNotDomainAdminException extends GdcRuntimeException {
    public UserNotDomainAdminException(final String message) {
        super(message);
    }

    public UserNotDomainAdminException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
