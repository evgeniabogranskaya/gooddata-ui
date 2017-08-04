/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.gooddata.test.ssh.CommandResult;

import static org.apache.commons.lang3.Validate.notNull;

public class AuditLogWrapperException extends RuntimeException {

    private final CommandResult result;

    public AuditLogWrapperException(final CommandResult result) {
        super("Unable to execute " + notNull(result, "result").toString());
        this.result = result;
    }

    public CommandResult getResult() {
        return result;
    }
}
