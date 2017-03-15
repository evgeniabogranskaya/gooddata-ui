/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

/**
 * Event writer.
 */
interface AuditLogEventWriter extends AutoCloseable {

    /**
     * Write a single event
     * @param event event
     */
    void logEvent(AuditLogEvent event);

    default void close() throws Exception {};
}
