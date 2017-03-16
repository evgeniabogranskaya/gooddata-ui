/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import static org.apache.commons.lang3.Validate.noNullElements;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Formats Audit Events as one-line JSON and writes them into the given file/writer.
 */
class AuditLogEventFileWriter extends AuditLogEventWriterBase {

    private static final File DEFAULT_DIR = new File("/mnt/log/cfal");

    private final File logFile;

    AuditLogEventFileWriter(final File logFile) throws IOException {
        super(createWriter(logFile));
        this.logFile = logFile;
    }

    AuditLogEventFileWriter(final File directory, final String... component) throws IOException {
        this(createLogFileName(directory, component));
    }

    /**
     * Creates a new instance logging into a file in the default directory {@link #DEFAULT_DIR}
     * with filename constructed from the component(s) name
     * @param component list of names
     * @throws IOException if the file can't be created
     * @see #createLogFileName(File, String...)
     */
    AuditLogEventFileWriter(final String... component) throws IOException {
        this(DEFAULT_DIR, component);
    }

    File getLogFile() {
        return logFile;
    }

    private static Writer createWriter(final File logFile) throws IOException {
        notNull(logFile, "logFile");
        try {
            return new FileWriter(logFile, true);
        } catch (IOException e) {
            throw new IOException("Unable to write file: " + logFile.getAbsolutePath(), e);
        }
    }

    private static File createLogFileName(final File directory, final String... component) {
        notNull(directory, "directory");
        noNullElements(component, "component");
        Arrays.stream(component).forEach(Validate::notEmpty);

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + directory.getAbsolutePath());
        }

        return new File(directory, String.join("-", component) + ".log");
    }

}
