/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.gooddata.test.ssh.CommandResult;
import com.gooddata.test.ssh.SshClient;

import java.io.StringWriter;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Wrapper for remote adding of events to the log file
 */
public class RemoteAuditLogFile {

    private static final String LOG_FILE = "/mnt/log/cfal/test.log";

    private final SshClient ssh;

    public RemoteAuditLogFile(final SshClient ssh) {
        this.ssh = notNull(ssh, "ssh");
    }

    public CommandResult appendEvent(final AuditLogEvent event) {
        final String string = asString(event);
        // have to use tee here because sudo is not able to append to file directly
        // eg simple "sudo echo aaa >> /foo" doesn't work
        final CommandResult result = ssh.execCmd(
                "echo",
                "'" + string + "'",
                "|",
                "sudo tee --append",
                LOG_FILE,
                "> /dev/null"
        );
        if (result.getExitCode() != 0) {
            throw new AuditLogWrapperException(result);
        }
        return result;
    }

    static String asString(final AuditLogEvent event) {
        notNull(event, "event");
        final StringWriter writer = new StringWriter();
        event.setComponent("component");
        new AuditLogEventWriterBase(writer).logEvent(event);
        return writer.toString();
    }
}
