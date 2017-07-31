/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal;

/**
 * Audit Log Service for emitting Audit Events from a client application
 */
public interface AuditLogService {

    /**
     * Send an Audit Log Event
     * @param event the event
     */
    void logEvent(AuditLogEvent event);

    /**
     * Number of errors during write.
     *
     * This method can be used for getting number of errors
     * and reporting them to the to grafana using Gauge
     * like this:
     * <code>
     * Gauge gauge = new Gauge() {
     *              @Override
     *              public Object getValue() {
     *                  return auditLogService.getErrorCount();
     *              }
     *}
     * </code>
     */
    long getErrorCount();
}
