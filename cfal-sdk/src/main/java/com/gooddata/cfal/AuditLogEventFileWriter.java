/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import static org.apache.commons.lang3.Validate.noNullElements;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Formats Audit Events as one-line JSON and writes them into the given file/writer.
 */
class AuditLogEventFileWriter implements AuditLogEventWriter {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogEventFileWriter.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final File DEFAULT_DIR = new File("/mnt/log/cfal");

    private final BufferedWriter writer;

    AuditLogEventFileWriter(final Writer writer) {
        this.writer = new BufferedWriter(notNull(writer, "writer"));
    }

    AuditLogEventFileWriter(final File logFile) throws IOException {
        this(createWriter(logFile));
    }

    /**
     * Creates a new instance logging into a file in the default directory {@link #DEFAULT_DIR}
     * with filename constructed from the component(s) name
     * @param component list of names
     * @throws IOException if the file can't be created
     * @see #createLogFileName(File, String...)
     */
    AuditLogEventFileWriter(final String... component) throws IOException {
        this(createLogFileName(DEFAULT_DIR, component));
    }

    @Override
    public void logEvent(final AuditLogEvent event) {
        try {
            final String eventData = format(event);
            writer.write(eventData);
            writer.flush();
        } catch (IOException e) {
            logger.error("Unable to write event={}", event.getType(), e);
        }
    }

    /**
     * Prepares event as a string ready to be written to the output log.
     * @param event event
     * @return single line string including the trailing newline
     */
    static String format(final AuditLogEvent event) throws JsonProcessingException {
        notNull(event, "event");
        notEmpty(event.getComponent(), "event.component");
        return OBJECT_MAPPER.writeValueAsString(event) + "\n";
    }

    @Override
    public void close() throws Exception {
        writer.close();
    }

    private static Writer createWriter(final File logFile) throws IOException {
        notNull(logFile, "logFile");
        try {
            return new FileWriter(logFile, true);
        } catch (IOException e) {
            throw new IOException("Unable to write file: " + logFile.getAbsolutePath(), e);
        }
    }

    static File createLogFileName(final File directory, final String... component) {
        notNull(directory, "directory");
        noNullElements(component, "component");
        Arrays.stream(component).forEach(Validate::notEmpty);

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + directory.getAbsolutePath());
        }

        return new File(directory, String.join("-", component) + ".log");
    }

}
