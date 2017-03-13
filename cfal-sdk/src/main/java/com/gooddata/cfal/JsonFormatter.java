/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Util class for json formatting
 */
class JsonFormatter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Prepares event as a string ready to be written to the output log.
     * @param event event
     * @return single line string including the trailing newline
     */
    static String format(final AuditLogEvent event) {
        notNull(event, "event");
        notEmpty(event.getComponent(), "event.component");

        try {
            return OBJECT_MAPPER.writeValueAsString(event) + "\n";
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e); // todo
        }
    }
}
