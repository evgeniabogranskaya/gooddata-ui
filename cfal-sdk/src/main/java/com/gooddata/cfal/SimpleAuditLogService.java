/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.springframework.beans.factory.DisposableBean;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.apache.commons.lang3.Validate.notNull;

/**
 * Single threaded blocking audit log writing service.
 */
public class SimpleAuditLogService extends AbstractAuditLogService implements DisposableBean {

    private static final File DEFAULT_DIR = new File("/mnt/log/cfal");

    private final BufferedWriter writer;

    public SimpleAuditLogService(final String component) throws IOException {
        this(component, DEFAULT_DIR);
    }

    public SimpleAuditLogService(final String component, final File directory) throws IOException {
        super(component);
        notNull(directory, "directory");

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + directory.getAbsolutePath());
        }
        final File logFile = new File(directory, component + ".log");
        try {
            writer = new BufferedWriter(new FileWriter(logFile, true));
        } catch (IOException e) {
            throw new IOException("Unable to write file: " + logFile.getAbsolutePath(), e);
        }
    }

    @Override
    protected void logEvent(final String eventData) {
        try {
            writer.write(eventData);
            writer.flush();
        } catch (IOException e) {
            logger.error("Unable to write data={}", eventData, e);
        }
    }

    @Override
    public void destroy() throws Exception {
        writer.close();
    }
}
