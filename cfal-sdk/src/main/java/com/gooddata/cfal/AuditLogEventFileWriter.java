/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import static org.apache.commons.lang3.Validate.noNullElements;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Formats Audit Events as one-line JSON and writes them into the given file/writer.
 * <p>
 * This class is not internally synchronized, if accessed from multiple threads it must be synchronized externally.
 */
class AuditLogEventFileWriter implements AuditLogEventWriter {

    private static final File DEFAULT_DIR = new File("/mnt/log/cfal");
    private static final String ROTATED_FILE_NAME_SUFFIX = "-old";
    private static final int DEFAULT_MAX_BYTES = 1024 * 1024 * 100;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final File logFile;

    private AuditLogEventWriterBase writer;
    private long maxBytes;
    private long writtenBytes;

    AuditLogEventFileWriter(final File logFile, final int maxBytes) throws IOException {
        this.writer = createWriter(logFile);
        this.logFile = logFile;
        this.writtenBytes = logFile.length();
        this.maxBytes = maxBytes;
    }

    AuditLogEventFileWriter(final File logFile) throws IOException {
        this(logFile, DEFAULT_MAX_BYTES);
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

    @Override
    public int logEvent(final AuditLogEvent event) {
        if (writtenBytes > maxBytes) {
            rotate();
            writtenBytes = 0;
        }

        final int bytes = writer.logEvent(event);
        writtenBytes += bytes;
        return bytes;
    }

    private void rotate() {
        logger.info("action=rotate status=start file={}", logFile);

        final File oldFile = new File(logFile.getAbsolutePath() + ROTATED_FILE_NAME_SUFFIX);
        if (oldFile.exists()) {
            if (oldFile.delete()) {
                logger.debug("action=rotate subaction=delete_old file={}", oldFile);
            } else {
                logger.error("action=rotate status=error subaction=delete_old file={}", oldFile);
                return;
            }
        }

        if (logFile.renameTo(oldFile)) {
            logger.debug("action=rotate subaction=rename source={} target={}", logFile, oldFile);
        } else {
            logger.error("action=rotate status=error subaction=rename source={} target={}", logFile, oldFile);
            return;
        }

        final AuditLogEventWriterBase newWriter;
        try {
            newWriter = createWriter(logFile);
        } catch (IOException e) {
            logger.error("action=rotate status=error create newfile={}", logFile, e);
            return;
        }

        try {
            writer.close();
        } catch (Exception e) {
            logger.warn("action=rotate subaction=close file={}", logFile, e);
        }
        writer = newWriter;
        logger.info("action=rotate status=finished file={}", logFile);
    }

    File getLogFile() {
        return logFile;
    }

    private static AuditLogEventWriterBase createWriter(final File logFile) throws IOException {
        notNull(logFile, "logFile");
        try {
            final Writer result = new FileWriter(logFile, true);
            return new AuditLogEventWriterBase(result);
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
