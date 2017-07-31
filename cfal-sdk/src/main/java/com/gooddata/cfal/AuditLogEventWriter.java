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
     * @return number of characters written
     */
    int logEvent(AuditLogEvent event);

    /**
     * Number of errors during write
     */
    long getErrorCounter();

    @Override
    default void close() throws Exception {}
}
