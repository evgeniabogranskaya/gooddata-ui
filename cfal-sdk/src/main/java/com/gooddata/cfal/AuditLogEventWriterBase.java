/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicLong;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

class AuditLogEventWriterBase implements AuditLogEventWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final BufferedWriter writer;

    private final AtomicLong errorCounter = new AtomicLong();

    public AuditLogEventWriterBase(final Writer writer) {
        this.writer = new BufferedWriter(notNull(writer, "writer"));
    }

    @Override
    public int logEvent(final AuditLogEvent event) {
        try {
            final String eventData = format(event);
            writer.write(eventData);
            writer.flush();
            return eventData.length();
        } catch (IOException e) {
            logger.error("Unable to write event={}", event.getType(), e);
            errorCounter.incrementAndGet();
            return 0;
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

    /**
     *
     * @return Number of errors during write operation
     */
    public long getErrorCount() {
        return errorCounter.get();
    }
}
